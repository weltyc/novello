package com.welty.novello.selfplay;

import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.BitBoard;
import com.welty.novello.solver.BitBoardUtils;
import com.welty.novello.solver.Square;
import org.jetbrains.annotations.NotNull;

/**
 */
public class EvalPlayer extends EndgamePlayer {
    final Eval eval;
    private final int searchDepth;

    public EvalPlayer(Eval eval) {
        this(eval, 1);
    }

    public EvalPlayer(Eval eval, int searchDepth) {
        this.eval = eval;
        this.searchDepth = searchDepth;
    }

    @Override public int calcMove(@NotNull BitBoard board, long moverMoves, int flags) {
        if (board.nEmpty() > 8) {
            return searchMove(board.mover(), board.enemy(), moverMoves, searchDepth, flags);
        } else {
            return solveMove(board);
        }
    }

    /**
     * Find the best move in a position using tree search.
     * <p/>
     * Precondition: The mover is guaranteed to have a move.
     *
     * @param mover      mover disks
     * @param enemy      enemy disks
     * @param moverMoves mover legal moves
     * @param depth      remaining search depth
     * @return index of square of best move
     */
    private int searchMove(long mover, long enemy, long moverMoves, int depth, int flags) {
        int bestMove = -1;
        int alpha = NO_MOVE;
        final String indent = indent(depth);

        while (moverMoves != 0) {
            final int sq = Long.numberOfTrailingZeros(moverMoves);
            final long placement = 1L << sq;
            moverMoves ^= placement;
            final Square square = Square.of(sq);
            final long flips = square.calcFlips(mover, enemy);
            final long subEnemy = mover | placement | flips;
            final long subMover = enemy & ~flips;
            if (0 != (flags & FLAG_PRINT_SEARCH)) {
                System.out.format("%s[%d] (%+5d,%+5d) scoring(%s):\n", indent, depth, NO_MOVE, -alpha, BitBoardUtils.sqToText(sq));
            }
            final int subScore = -searchScore(subMover, subEnemy, NO_MOVE, -alpha, depth - 1);
            if (0 != (flags & FLAG_PRINT_SEARCH)) {
                System.out.format("%s[%d] (%+5d,%+5d) score(%s)=%+5d\n", indent, depth, NO_MOVE, -alpha, BitBoardUtils.sqToText(sq), subScore);
            }
            if (subScore > alpha) {
                bestMove = sq;
                alpha = subScore;
            }
        }

        if (0 != (flags & FLAG_PRINT_SCORE)) {
            System.out.println();
            System.out.format("%s score = %+5d (%s)\n", this, alpha, BitBoardUtils.sqToText(bestMove));
        }
        return bestMove;
    }

    /**
     * Score a position using tree search.
     * <p/>
     * The mover is not guaranteed to have a move.
     * <p/>
     * See {@link #searchScore(long, long, long, int, int, int)}  for parameter and output details.
     */
    private int searchScore(long mover, long enemy, int alpha, int beta, int depth) {
        if (depth == 0) {
            return score(mover, enemy);
        }
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        if (moverMoves != 0) {
            return searchScore(mover, enemy, moverMoves, alpha, beta, depth);
        } else {
            final long enemyMoves = BitBoardUtils.calcMoves(mover, enemy);
            if (enemyMoves != 0) {
                return -searchScore(enemy, mover, enemyMoves, -beta, -alpha, depth);
            } else {
                return CoefficientCalculator.DISK_VALUE * (Long.bitCount(mover) - Long.bitCount(enemy));
            }
        }
    }

    /**
     * This is the score used when no move has yet been evaluated. It needs to be lower than
     * any valid score.
     * <p/>
     * It is sometimes used as alpha in a search. When switching sides, the search uses -alpha as the new beta.
     * This means NO_MOVE can't be Integer.MIN_VALUE, because -Integer.MIN_VALUE = Integer.MIN_VALUE and we would
     * end up with new beta < new alpha.
     */
    private static final int NO_MOVE = -Integer.MAX_VALUE;


    /**
     * Score a position using tree search.
     * <p/>
     * Precondition: The mover is guaranteed to have a move.
     *
     * @param mover      mover disks
     * @param enemy      enemy disks
     * @param moverMoves mover legal moves
     * @param alpha      search alpha
     * @param beta       search beta
     * @param depth      remaining search depth
     * @return score
     */
    private int searchScore(long mover, long enemy, long moverMoves, int alpha, int beta, int depth) {
        int result = NO_MOVE;

//        final String indent = indent(depth);
//        System.out.format("%s[%d] (%+5d,%+5d) -->\n", indent, depth, alpha, beta);

        while (moverMoves != 0) {
            final int sq = Long.numberOfTrailingZeros(moverMoves);
            final long placement = 1L << sq;
            moverMoves ^= placement;
            final Square square = Square.of(sq);
            final long flips = square.calcFlips(mover, enemy);
            final long subEnemy = mover | placement | flips;
            final long subMover = enemy & ~flips;
            final int subScore = -searchScore(subMover, subEnemy, -beta, -alpha, depth - 1);
//            System.out.format("%s[%d] (%+5d,%+5d) score(%s)=%+5d\n", indent, depth, -beta, -alpha, BitBoardUtils.sqToText(sq), subScore);
            if (subScore >= beta) {
                return subScore;
            }
            if (subScore > result) {
                result = subScore;
                if (subScore > alpha) {
                    alpha = subScore;
                }
            }
        }

        return result;
    }

    private int score(long mover, long enemy) {
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        final int moveScore;
        if (moverMoves != 0) {
            moveScore = eval.eval(mover, enemy, moverMoves, enemyMoves);
        } else if (enemyMoves != 0) {
            moveScore = -eval.eval(enemy, mover, enemyMoves, moverMoves);
        } else {
            moveScore = CoefficientCalculator.DISK_VALUE * (Long.bitCount(mover) - Long.bitCount(enemy));
        }
        return moveScore;
    }

    @Override public String toString() {
        return eval.toString() + ":" + searchDepth;
    }

    private String indent(int depth) {
        final StringBuilder sb = new StringBuilder();
        for (int i = depth; i < searchDepth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }
}

