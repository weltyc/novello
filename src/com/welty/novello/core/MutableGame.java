package com.welty.novello.core;

import com.orbanova.common.misc.Require;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Game history
 */
public class MutableGame {
    public final @NotNull Position startPosition;
    public final @NotNull String blackName;
    public final @NotNull String whiteName;
    public final @NotNull String place;

    /**
     * A list of all moves played in the game, including passes.
     */
    private final List<Move> moves = new ArrayList<>();
    private boolean isOver = false;
    private Position lastPosition;

    public MutableGame(@NotNull Position startPosition, @NotNull String blackName, @NotNull String whiteName, @NotNull String place) {
        this.startPosition = startPosition;
        lastPosition = startPosition;
        this.blackName = blackName;
        this.whiteName = whiteName;
        this.place = place;
    }

    /**
     * @return a list of all moves played in the game, including passes
     */
    public List<Move> getMoves() {
        return new ArrayList<>(moves);
    }

    public String toGgf() {
        final StringBuilder sb = new StringBuilder();
        sb.append("(;GM[Othello]");
        sb.append("PC[").append(place).append("]");
        sb.append("PB[").append(blackName).append("]");
        sb.append("PW[").append(whiteName).append("]");
        sb.append("RE[").append(isOver ? netScore() : "?").append("]");
        sb.append("TI[0]");
        sb.append("TY[8r]");

        sb.append("BO[8 ").append(startPosition.positionString()).append("]");
        Position cur = startPosition;
        for (Move move : moves) {
            sb.append(cur.blackToMove ? "B[" : "W[");
            move.appendTo(sb);
            sb.append(']');
            cur = cur.playOrPass(move.sq);
        }

        sb.append(";)");
        return sb.toString();
    }

    /**
     * Play a move
     *
     * @param moveText in GGF format. Square [/eval[/time]]
     */
    public void play(String moveText) {
        play(new Move(moveText));
    }

    /**
     * Add a move
     *
     * @param moveScore move and score of the move
     * @param time      time taken to make the move, in seconds
     */
    public void play(MoveScore moveScore, double time) {
        play(new Move(moveScore, time));
    }

    public void pass() {
        play(Move.PASS);
    }

    public void finish() {
        if (!moves.isEmpty() && moves.get(moves.size() - 1).isPass()) {
            throw new IllegalArgumentException("Can't end on a pass");
        }
        this.isOver = true;
    }

    private void play(Move move) {
        moves.add(move);
        lastPosition = lastPosition.playOrPass(move.sq);
    }

    public @NotNull Position getStartPosition() {
        return startPosition;
    }

    public Position getLastPosition() {
        return lastPosition;
    }

    /**
     * @return a list of PositionValues, but only those where the mover has a legal move.
     */
    public List<PositionValue> calcPositionValues() {
        final int netScore = getLastPosition().terminalScore();
        final List<PositionValue> pvs = new ArrayList<>();
        Position pos = getStartPosition();
        for (Move move : moves) {
            if (move.isPass()) {
                pos = pos.pass();
            } else {
                pvs.add(pv(pos, netScore));
                pos = pos.play(move.sq);
            }
        }
        return pvs;
    }

    private static PositionValue pv(Position pos, int netScore) {
        return new PositionValue(pos.mover(), pos.enemy(), pos.blackToMove ? netScore : -netScore);
    }

    /**
     * @return number of black disks - number of white disks at the end of the game
     */
    public int netScore() {
        return lastPosition.terminalScore();
    }

    /**
     * Creates a new MutableGame from the ggf text.
     *
     * @param ggf text of ggf game.
     * @return new MutableGame
     * @throws IllegalArgumentException if game is not in GGF format, or board is not 8x8
     */
    public static MutableGame ofGgf(String ggf) {
        ggf = ggf.trim();
        // find and strip GGF identifiers
        if (!ggf.startsWith("(;") || !ggf.endsWith(";)")) {
            throw new IllegalArgumentException("not a GGF format game");
        }
        ggf = ggf.substring(2, ggf.length() - 2);
        final HashMap<String, String> tags = getGgfTags(ggf);


        final String place = getRequiredTag(tags, "PC");
        final String blackName = getRequiredTag(tags, "PB");
        final String whiteName = getRequiredTag(tags, "PW");
        final String bo = getRequiredTag(tags, "BO");
        if (!bo.startsWith("8 ")) {
            throw new IllegalArgumentException("We can only handle 8x8 boards.");
        }
        final Position startPosition = Position.of(bo.substring(2));

        final MutableGame game = new MutableGame(startPosition, blackName, whiteName, place);

        // add moves
        int loc = 0;
        for (; ; ) {
            final int tagEnd = ggf.indexOf('[', loc);
            if (tagEnd < 0) {
                break;
            }
            final int valueEnd = ggf.indexOf(']', tagEnd);
            if (valueEnd < 0) {
                throw new IllegalArgumentException("malformed GGF game");
            }
            final String tag = ggf.substring(loc + 1, tagEnd).trim();
            final String value = ggf.substring(tagEnd + 1, valueEnd).trim();
            if (tag.equals("B") || tag.equals("W")) {
                game.play(new Move(value));
            }
            loc = valueEnd;
        }

        return game;
    }

    private static HashMap<String, String> getGgfTags(String ggf) {
        // get tags from GGF
        final HashMap<String, String> tags = new HashMap<>();
        int loc = 0;
        for (; ; ) {
            final int tagEnd = ggf.indexOf('[', loc);
            if (tagEnd < 0) {
                break;
            }
            final int valueEnd = ggf.indexOf(']', tagEnd);
            if (valueEnd < 0) {
                throw new IllegalArgumentException("malformed GGF game");
            }
            final String tag = ggf.substring(loc + 1, tagEnd).trim();
            final String value = ggf.substring(tagEnd + 1, valueEnd).trim();
            tags.put(tag, value);
            loc = valueEnd;
        }
        return tags;
    }

    private static String getRequiredTag(HashMap<String, String> tags, String tag) {
        final String value = tags.get(tag);
        if (value == null) {
            throw new IllegalArgumentException("GGF missing tag: " + tag);
        }
        return value;
    }

    /**
     * Calculate the position with the given number of empties.
     * <p/>
     * If there is a pass at that number of empties, the position with the player-to-move having a legal move is returned.
     * If the game is over, the terminal position is returned. If there is no position with that number of empties,
     * null is returned.
     *
     * @param nEmpty number of empty disks in the position to find
     */
    public @Nullable Position calcPositionAt(int nEmpty) {
        Position pos = getStartPosition();
        for (Move move : moves) {
            if (move.isPass()) {
                pos = pos.pass();
            } else {
                if (pos.nEmpty() == nEmpty) {
                    return pos;
                }
                pos = pos.play(move.sq);
            }
        }
        if (pos.nEmpty() == nEmpty) {
            return pos;
        } else {
            return null;
        }
    }

    /**
     * Construct a game from the very old Ntest game format.
     * <p/>
     * An example is
     * -WZebra   +00 d16      EML=4B:TJ,532+$"%*-K>F#?S6][\^UN!Z7/RYOIGW19@80AQHXP_V'&. ()
     *
     * @param s game string in very old Ntest game format
     * @return MutableGame
     */
    public static MutableGame ofVong(String s) {
        final String blackName = s.substring(1, 9).trim();
        final String whiteName = s.substring(14, 22).trim();
        final MutableGame game = new MutableGame(Position.START_POSITION, blackName, whiteName, "Vong");

        final char[] moves = s.substring(23).toCharArray();
        for (char c : moves) {
            if (game.getLastPosition().calcMoves() == 0) {
                game.pass();
            }
            game.play(BitBoardUtils.sqToText(c - ' '));
        }
        return game;

    }

    /**
     * Get the position after nMoves moves.
     * <p/>
     * Passes count as moves.
     *
     * @param nMoves number of moves to go forward.
     * @return the position.
     */
    public Position getPositionAfter(int nMoves) {
        Require.geq(nMoves, 0);
        Require.leq(nMoves, "nMove", moves.size(), "total moves in the game");

        Position pos = getStartPosition();
        for (int i = 0; i < nMoves; i++) {
            pos = pos.playOrPass(moves.get(i).sq);
        }
        return pos;
    }

    /**
     * Calculate time taken by a player
     *
     * @param blackPlayer if true, return time taken by the black player; otherwise return time taken by the white player.
     * @return total time taken by a player in all recorded moves. Does not include any time taken since the last move.
     */
    public double time(boolean blackPlayer) {
        boolean counts = getStartPosition().blackToMove == blackPlayer;
        double time = 0;
        for (Move move : moves) {
            if (counts) {
                time += move.time;
            }
            counts = !counts;
        }
        return time;
    }
}
