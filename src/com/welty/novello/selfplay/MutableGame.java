package com.welty.novello.selfplay;

import com.welty.novello.solver.BitBoard;
import com.welty.novello.solver.BitBoardUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Game history
 */
public class MutableGame {
    private final List<Move> moves = new ArrayList<>();
    private boolean isComplete = false;
    private BitBoard lastPosition;
    private final BitBoard startPosition;

    public MutableGame(BitBoard startPosition) {
        this.startPosition = startPosition;
        lastPosition = startPosition;
    }

    public String toGgf() {
        final StringBuilder sb = new StringBuilder();
        sb.append("(;GM[Othello]");

        sb.append("BO[8 ").append(startPosition.positionString()).append("]");
        BitBoard cur = startPosition;
        for (Move move : moves) {
            sb.append(cur.blackToMove ? "B[" : "W[");
            move.appendTo(sb);
            sb.append(']');
            cur = cur.play(move.sq);
        }

        sb.append(";)");
        return sb.toString();
    }

    public void play(String squareText) {
        play(new Move(squareText));
    }

    public void play(MoveScore moveScore) {
        play(new Move(moveScore, 0));
    }

    public void pass() {
        play(Move.PASS);
    }


    private void play(Move move) {
        moves.add(move);
        lastPosition = lastPosition.play(move.sq);
    }

    public BitBoard getStartPosition() {
        return startPosition;
    }

    public BitBoard getLastPosition() {
        return lastPosition;
    }

    private static class Move {
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
         *
         * This is a pass move with no eval and no time elapsed.
         * To create a pass move with an eval or time elapsed, use the constructor.
         */
        static final Move PASS = new Move("PASS");

        Move(String text) {
            final String[] split = text.split("/");
            if (split.length > 3) {
                throw new IllegalArgumentException("Moves may have at most 3 components");
            }
            sq = split[0].startsWith("PA")?-1:BitBoardUtils.textToSq(split[0]);
            time = (split.length > 1 && !split[1].isEmpty()) ? Double.parseDouble(split[1]) : 0;
            eval = (split.length > 2 && !split[2].isEmpty()) ? Double.parseDouble(split[2]) : 0;
        }

        public Move(MoveScore moveScore, double time) {
            this.sq = moveScore.sq;
            this.eval = moveScore.score;
            this.time = time;
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder();
            appendTo(sb);
            return sb.toString();
        }

        private void appendTo(StringBuilder sb) {
            sb.append(BitBoardUtils.sqToText(sq));
            if (time != 0 || eval != 0) {
                sb.append('/');
                if (time != 0) {
                    sb.append(time);
                }
                if (eval != 0) {
                    sb.append('/');
                    sb.append(eval);
                }
            }
        }
    }
}
