package com.welty.novello.core;

import com.orbanova.common.misc.Require;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.othello.gdk.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Game history
 */
public class MutableGame {
    private final @NotNull State startState;
    public final @NotNull String blackName;
    public final @NotNull String whiteName;
    public final @NotNull String place;

    /**
     * A list of all moves played in the game, including passes.
     */
    private final List<Move8x8> mlis = new ArrayList<>();
    private boolean isOver = false;
    private Position lastPosition;

    public MutableGame(@NotNull Position startPosition, @NotNull String blackName, @NotNull String whiteName, @NotNull String place) {
        this(startPosition, blackName, whiteName, place, OsClock.DEFAULT, OsClock.DEFAULT);
    }

    public MutableGame(@NotNull Position startPosition, @NotNull String blackName, @NotNull String whiteName
            , @NotNull String place, @NotNull OsClock blackClock, @NotNull OsClock whiteClock) {
        this.startState = new State(startPosition, blackClock, whiteClock);
        lastPosition = startPosition;
        this.blackName = blackName;
        this.whiteName = whiteName;
        this.place = place;
    }

    /**
     * @return a list of all moves played in the game, including passes
     */
    public List<Move8x8> getMlis() {
        return new ArrayList<>(mlis);
    }

    public String toGgf() {
        final StringBuilder sb = new StringBuilder();
        sb.append("(;GM[Othello]");
        sb.append("PC[").append(place).append("]");
        sb.append("PB[").append(blackName).append("]");
        sb.append("PW[").append(whiteName).append("]");
        sb.append("RE[").append(isOver ? netScore() : "?").append("]");
        if (startState.blackClock == startState.whiteClock) {
            sb.append("TI[").append(startState.blackClock).append("]");
        } else {
            sb.append("TB[").append(startState.blackClock).append("]");
            sb.append("TW[").append(startState.whiteClock).append("]");
        }
        sb.append("TY[8r]");

        sb.append("BO[8 ").append(startState.position.positionString()).append("]");
        Position cur = startState.position;
        for (Move8x8 move : mlis) {
            sb.append(cur.blackToMove ? "B[" : "W[");
            sb.append(move);
            sb.append(']');
            cur = cur.playOrPass(move.getSq());
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
        play(new Move8x8(moveText));
    }

    /**
     * Add a move
     *
     * @param moveScore move and score of the move
     * @param time      time taken to make the move, in seconds
     */
    public void play(MoveScore moveScore, double time) {
        play(new Move8x8(moveScore, time));
    }

    public void pass() {
        play(Move8x8.PASS);
    }

    public void finish() {
        if (!mlis.isEmpty() && mlis.get(mlis.size() - 1).isPass()) {
            throw new IllegalArgumentException("Can't end on a pass");
        }
        this.isOver = true;
    }

    private void play(Move8x8 move) {
        mlis.add(move);
        lastPosition = lastPosition.playOrPass(move.getSq());
    }

    public @NotNull Position getStartPosition() {
        return startState.position;
    }

    public Position getLastPosition() {
        return lastPosition;
    }

    /**
     * @return a list of PositionValues, but only those where the mover has a legal move.
     */
    public List<MeValue> calcPositionValues() {
        final int netScore = getLastPosition().terminalScore();
        final List<MeValue> pvs = new ArrayList<>();
        Position pos = getStartPosition();
        for (Move8x8 move : mlis) {
            if (move.isPass()) {
                pos = pos.pass();
            } else {
                pvs.add(pv(pos, netScore));
                pos = pos.play(move.getSq());
            }
        }
        return pvs;
    }

    private static MeValue pv(Position pos, int netScore) {
        final int centidisks = CoefficientCalculator.DISK_VALUE * (pos.blackToMove ? netScore : -netScore);
        return new MeValue(pos.mover(), pos.enemy(), centidisks);
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
        final COsGame osGame = new COsGame(ggf);


        return of(osGame);
    }


    /**
     * Creates a new MutableGame from the OsGame.
     *
     * @param osGame input.
     * @throws IllegalArgumentException if the board is not 8x8.
     */
    public static MutableGame of(@NotNull COsGame osGame) {
        final String place = osGame.sPlace;
        OsPlayerInfo black = osGame.getBlackPlayer();
        final String blackName = black.name;
        final String whiteName = osGame.getWhitePlayer().name;
        final OsBoardType bt = osGame.getStartPosition().board.getBoardType();
        if (bt.n != 8) {
            throw new IllegalArgumentException("MutableGame is required to be on an 8x8 board, but was " + bt + ".");
        }
        final OsClock blackClock = osGame.getStartPosition().getBlackClock();
        final OsClock whiteClock = osGame.getStartPosition().getWhiteClock();

        final Position startPosition = Position.of(osGame.posStart.board);

        final MutableGame game = new MutableGame(startPosition, blackName, whiteName, place, blackClock, whiteClock);

        final COsMoveList moveList = osGame.getMoveList();
        for (OsMoveListItem mli : moveList) {
            game.play(new Move8x8(mli));
        }

        return game;
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
        for (Move8x8 move : mlis) {
            if (move.isPass()) {
                pos = pos.pass();
            } else {
                if (pos.nEmpty() == nEmpty) {
                    return pos;
                }
                pos = pos.play(move.getSq());
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
     * Calculate time taken by a player
     *
     * @param blackPlayer if true, return time taken by the black player; otherwise return time taken by the white player.
     * @return total time taken by a player in all recorded moves. Does not include any time taken since the last move.
     */
    public double time(boolean blackPlayer) {
        boolean counts = getStartPosition().blackToMove == blackPlayer;
        double time = 0;
        for (Move8x8 move : mlis) {
            if (counts) {
                time += move.getElapsedTime();
            }
            counts = !counts;
        }
        return time;
    }

    /**
     * Get the State at the beginning of the game.
     *
     * @return game start State
     */
    @NotNull public State getStartState() {
        return startState;
    }

    public @NotNull State getStateAfter(int nMoves) {
        Require.geq(nMoves, 0);
        Require.leq(nMoves, "nMove", mlis.size(), "total moves in the game");

        State state = getStartState();
        for (int i = 0; i < nMoves; i++) {
            state = state.playOrPass(mlis.get(i));
        }
        return state;
    }

    @Override public String toString() {
        return toGgf();
    }
}
