package com.welty.novello.core;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Game history
 */
public class MutableGame {
    public final Position startPosition;
    public final String blackName;
    public final String whiteName;
    public final String place;

    private final List<Move> moves = new ArrayList<>();
    private boolean isOver = false;
    private Position lastPosition;

    public MutableGame(Position startPosition, String blackName, String whiteName, String place) {
        this.startPosition = startPosition;
        lastPosition = startPosition;
        this.blackName = blackName;
        this.whiteName = whiteName;
        this.place = place;
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

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getLastPosition() {
        return lastPosition;
    }

    /**
     * @return a list of PositionValues, but only those where the mover has a legal move.
     */
    public List<PositionValue> calcPositionValues() {
        final int netScore = getLastPosition().netDisks();
        final List<PositionValue> pvs = new ArrayList<>();
        Position pos = getStartPosition();
        for (MutableGame.Move move : moves) {
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
        return lastPosition.netDisks();
    }

    public static MutableGame ofGgf(String ggf) {
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
        final Position startPosition = new Position(bo.substring(2));

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
        for (MutableGame.Move move : moves) {
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


    static class Move {
        /**
         * Square of the move, or -1 if the move was a pass
         */
        final int sq;

        /**
         * Time taken, in seconds
         */
        final double time;

        /**
         * Evaluation returned by the engine
         */
        final double eval;

        /**
         * Generic pass move
         * <p/>
         * This is a pass move with no eval and no time elapsed.
         * To create a pass move with an eval or time elapsed, use the constructor.
         */
        static final Move PASS = new Move("PASS");

        Move(String text) {
            final String[] split = text.split("/");
            if (split.length > 3) {
                throw new IllegalArgumentException("Moves may have at most 3 components");
            }
            sq = split[0].toUpperCase().startsWith("PA") ? -1 : BitBoardUtils.textToSq(split[0]);
            eval = (split.length > 1 && !split[1].isEmpty()) ? Double.parseDouble(split[1]) : 0;
            time = (split.length > 2 && !split[2].isEmpty()) ? Double.parseDouble(split[2]) : 0;
        }

        public Move(MoveScore moveScore, double time) {
            this.sq = moveScore.sq;
            this.eval = moveScore.score * .01;
            this.time = time;
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder();
            appendTo(sb);
            return sb.toString();
        }

        private void appendTo(StringBuilder sb) {
            sb.append(isPass() ? "PASS" : BitBoardUtils.sqToText(sq));
            if (time != 0 || eval != 0) {
                sb.append('/');
                if (eval != 0) {
                    sb.append(String.format("%.2f", eval));
                }
                if (time != 0) {
                    sb.append('/');
                    sb.append(time);
                }
            }
        }

        public boolean isPass() {
            return sq < 0;
        }
    }
}
