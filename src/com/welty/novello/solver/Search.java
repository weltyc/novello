package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.core.Square;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Eval;
import org.jetbrains.annotations.NotNull;

/**
 * A reusable Search object.
 * <p/>
 * This class is not thread-safe.
 */
public class Search {
    public static final int FLAG_PRINT_SCORE = 1;
    public static final int FLAG_PRINT_SEARCH = 2;

    public Search(@NotNull Eval eval, int flags) {
        this.flags = flags;
        this.eval = eval;
    }

    /**
     * Get the number of times a move has been made (pieces have been flipped on the board).
     * <p/>
     * If called after the search has completed, returns the number of times a move was made during the search.
     * If called during the search, returns the number of moves so far.
     *
     * @return the number of times a move has been made since the most recent search start.
     */
    public long nFlips() {
        return nFlips;
    }

    /**
     * Select a move based on a midgame search.
     * <p/>
     * Precondition: The mover has at least one legal move.
     *
     * @param position   position to search
     * @param moverMoves legal moves to check. If this is a subset of all legal moves, only the subset will
     *                   be checked.
     * @param depth      search depth
     * @return the best move from this position, and its score in centi-disks
     */
    public MoveScore calcMove(Position position, long moverMoves, int depth) {
        this.rootDepth = depth;
        nFlips = 0;
        final BA ba = treeMove(position.mover(), position.enemy(), moverMoves, NO_MOVE, -NO_MOVE, depth);
        return new MoveScore(ba.bestMove, ba.score);
    }

    /**
     * Return a score estimate based on a midgame search.
     * <p/>
     * The mover does not need to have a legal move - if he doesn't this method will pass or return a terminal value as
     * necessary.
     *
     * @param position position to evaluate
     * @param depth    search depth. If &le; 0, returns the eval.
     * @return score of the move.
     */
    public int calcScore(Position position, int depth) {
        return calcScore(position.mover(), position.enemy(), depth);
    }

    /**
     * Return a score estimate based on a midgame search.
     * <p/>
     * The mover does not need to have a legal move - if he doesn't this method will pass or return a terminal value as
     * necessary.
     *
     * @param mover mover disks
     * @param enemy enemy disks
     * @param depth search depth. If &le; 0, returns the eval.
     * @return score of the move.
     */
    public int calcScore(long mover, long enemy, int depth) {
        nFlips = 0;
        this.rootDepth = depth;
        return searchScore(mover, enemy, NO_MOVE, -NO_MOVE, depth);
    }


    /**
     * This is the score used when no move has yet been evaluated. It needs to be lower than
     * any valid score. But it also needs to be well away from the bounds for an int, so we can add MPC margins to
     * it and not overflow.
     */
    static final int NO_MOVE = Integer.MIN_VALUE >> 1;

    private final int flags;
    private final @NotNull Eval eval;

    /**
     * Depth of the search passed to move() or score() by the client.
     */
    private int rootDepth;

    /**
     * Number of nodes completed by this Search since it was constructed
     */
    private long nFlips;

    static class BA {
        int bestMove;
        int score;
    }

    private static final long[] masks = {BitBoardUtils.CORNERS, ~(BitBoardUtils.CORNERS | BitBoardUtils.X_SQUARES), BitBoardUtils.X_SQUARES};

    /**
     * Find the best move in a position using tree search.
     * <p/>
     * Precondition: The mover is guaranteed to have a move. depth > 0.
     * <p/>
     * The return value is a structure containing a move square and a score
     * The score is a fail-soft alpha beta score. The move square will be -1 if score &lt; alpha
     * and will be a legal move if score >= alpha.
     *
     * @param mover      mover disks
     * @param enemy      enemy disks
     * @param moverMoves mover legal moves
     * @param depth      remaining search depth
     * @return BA see above
     */
    BA treeMove(long mover, long enemy, long moverMoves, int alpha, int beta, int depth) {
        assert beta > alpha;
        assert depth > 0;
        BA ba = new BA();
        ba.bestMove = -1;
        ba.score = NO_MOVE;

        if (depth > 2) {
            // internal iterative deepening
            final int iidMove = treeMove(mover, enemy, moverMoves, alpha, beta, depth>3?2:1).bestMove;
            if (iidMove >=0) {
                final int subScore = calcMoveScore(mover, enemy, alpha, beta, depth, iidMove);
                if (subScore > ba.score) {
                    ba.score = subScore;
                    if (subScore > alpha) {
                        ba.bestMove = iidMove;
                        alpha = subScore;
                        if (subScore >= beta) {
                            return ba;
                        }
                    }
                }
                moverMoves &=~ (1L<< iidMove);
            }
        }
        for (long mask : masks) {
            long movesToCheck = moverMoves & mask;
            while (movesToCheck != 0) {
                final int sq = Long.numberOfTrailingZeros(movesToCheck);
                final long placement = 1L << sq;
                movesToCheck ^= placement;
                final int subScore = calcMoveScore(mover, enemy, alpha, beta, depth, sq);
                if (subScore > ba.score) {
                    ba.score = subScore;
                    if (subScore > alpha) {
                        ba.bestMove = sq;
                        alpha = subScore;
                        if (subScore >= beta) {
                            return ba;
                        }
                    }
                }
            }
        }

        if (shouldPrintScore()) {
            System.out.println();
            System.out.format("score = %+5d (%s) with alpha = %d\n", ba.score, BitBoardUtils.sqToText(ba.bestMove), alpha);
        }
        return ba;
    }

    private int calcMoveScore(long mover, long enemy, int alpha, int beta, int depth, int sq) {
        final Square square = Square.of(sq);
        nFlips++;
        final long flips = square.calcFlips(mover, enemy);
        final long subEnemy = mover | (1L<<sq) | flips;
        final long subMover = enemy & ~flips;
        if (shouldPrintSearch()) {
            System.out.format("%s[%d] (%+5d,%+5d) scoring(%s):\n", indent(depth), depth, alpha, beta, BitBoardUtils.sqToText(sq));
        }
        final int subScore = -searchScore(subMover, subEnemy, -beta, -alpha, depth - 1);
        if (shouldPrintSearch()) {
            System.out.format("%s[%d] (%+5d,%+5d) score(%s)=%+5d\n", indent(depth), depth, alpha, beta, BitBoardUtils.sqToText(sq), subScore);
        }
        return subScore;
    }

    private boolean shouldPrintScore() {
        return 0 != (flags & FLAG_PRINT_SCORE);
    }

    private boolean shouldPrintSearch() {
        return 0 != (flags & FLAG_PRINT_SEARCH);
    }


    /**
     * Score a position using tree search.
     * <p/>
     * The caller does not need to ensure the mover has a move; if he does not, this routine will handle passing
     * or terminal valuation as necessary.
     *
     * @param depth remaining search depth. If depth &le; 0 this will return the eval.
     * @return score
     */
    private int searchScore(long mover, long enemy, int alpha, int beta, int depth) {
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        if (moverMoves != 0) {
//            final int score = treeScore(mover, enemy, moverMoves, alpha, beta, depth);
            final int score = treeScore(mover, enemy, moverMoves, alpha, beta, depth);
            return score;
        } else {
            final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
            if (enemyMoves != 0) {
                final int score = treeScore(enemy, mover, enemyMoves, -beta, -alpha, depth);
                return -score;
            } else {
                return CoefficientCalculator.DISK_VALUE * BitBoardUtils.terminalScore(mover, enemy);
            }
        }
    }

    int treeScore(long mover, long enemy, long moverMoves, int alpha, int beta, int depth) {
        if (depth <= 0) {
            return eval.eval(mover, enemy);
        }

        return treeMove(mover, enemy, moverMoves, alpha, beta, depth).score;
    }

    private String indent(int depth) {
        final StringBuilder sb = new StringBuilder();
        for (int i = depth; i < rootDepth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

}
