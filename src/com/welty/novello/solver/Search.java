package com.welty.novello.solver;

import com.welty.novello.core.*;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Mpc;
import org.jetbrains.annotations.NotNull;

/**
 * A reusable Search object.
 * <p/>
 * This class is not thread-safe.
 */
public class Search {
    public static final int FLAG_PRINT_SEARCH = 1;

    public Search(@NotNull Counter counter, int flags) {
        this.flags = flags;
        this.counter = counter;
    }

    /**
     *
     * @return node counts (flips, evals) since the search was constructed.
     */
    public Counts counts() {
        return counter.getNodeStats();
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
    public MoveScore calcMove(Position position, long moverMoves, int depth, boolean mpc) {
        this.rootDepth = depth;
        final BA ba = treeMove(position.mover(), position.enemy(), moverMoves, NovelloUtils.NO_MOVE, -NovelloUtils.NO_MOVE, depth, mpc);
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
     * @param mpc      if true, use MPC
     * @return score of the move.
     */
    public int calcScore(Position position, int depth, boolean mpc) {
        return calcScore(position.mover(), position.enemy(), depth, mpc);
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
     * @param mpc   if true, search uses MPC
     * @return score of the move.
     */
    public int calcScore(long mover, long enemy, int depth, boolean mpc) {
        this.rootDepth = depth;
        return searchScore(mover, enemy, NovelloUtils.NO_MOVE, -NovelloUtils.NO_MOVE, depth, mpc);
    }

    private final int flags;
    private final @NotNull Counter counter;

    /**
     * Depth of the search passed to move() or score() by the client.
     */
    private int rootDepth;

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
    BA treeMove(long mover, long enemy, long moverMoves, int alpha, int beta, int depth, boolean mpc) {
        assert beta > alpha;
        assert depth > 0;
        BA ba = new BA();
        ba.bestMove = -1;
        ba.score = NovelloUtils.NO_MOVE;

        if (depth > 2) {
            // internal iterative deepening
            final int iidMove = treeMove(mover, enemy, moverMoves, alpha, beta, depth > 3 ? 2 : 1, mpc).bestMove;
            if (iidMove >= 0) {
                final int subScore = calcMoveScore(mover, enemy, alpha, beta, depth, iidMove, mpc);
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
                moverMoves &= ~(1L << iidMove);
            }
        }
        for (long mask : masks) {
            long movesToCheck = moverMoves & mask;
            while (movesToCheck != 0) {
                final int sq = Long.numberOfTrailingZeros(movesToCheck);
                final long placement = 1L << sq;
                movesToCheck ^= placement;
                final int subScore = calcMoveScore(mover, enemy, alpha, beta, depth, sq, mpc);
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

        return ba;
    }

    private int calcMoveScore(long mover, long enemy, int alpha, int beta, int depth, int sq, boolean mpc) {
        final Square square = Square.of(sq);
        final long flips = counter.calcFlips(square, mover, enemy);
        final long subEnemy = mover | (1L << sq) | flips;
        final long subMover = enemy & ~flips;
        if (shouldPrintSearch()) {
            System.out.format("%s[%d] (%+5d,%+5d) scoring(%s):\n", indent(depth), depth, alpha, beta, BitBoardUtils.sqToText(sq));
        }
        final int subScore = -searchScore(subMover, subEnemy, -beta, -alpha, depth - 1, mpc);
        if (shouldPrintSearch()) {
            System.out.format("%s[%d] (%+5d,%+5d) score(%s)=%+5d\n", indent(depth), depth, alpha, beta, BitBoardUtils.sqToText(sq), subScore);
        }
        return subScore;
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
    private int searchScore(long mover, long enemy, int alpha, int beta, int depth, boolean mpc) {
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        if (moverMoves != 0) {
            final int score = treeScore(mover, enemy, moverMoves, alpha, beta, depth, mpc);
            return score;
        } else {
            final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
            if (enemyMoves != 0) {
                final int score = treeScore(enemy, mover, enemyMoves, -beta, -alpha, depth, mpc);
                return -score;
            } else {
                return CoefficientCalculator.DISK_VALUE * BitBoardUtils.terminalScore(mover, enemy);
            }
        }
    }

    int treeScore(long mover, long enemy, long moverMoves, int alpha, int beta, int depth, boolean mpc) {
        if (depth <= 0) {
            return counter.eval(mover, enemy);
        }

        if (mpc && depth >= 2) {
            final int nEmpty = BitBoardUtils.nEmpty(mover, enemy);
            Mpc.Cutter[] cutters = counter.mpcs.cutters(nEmpty, depth);
            for (Mpc.Cutter cutter : cutters) {
                final int shallowAlpha = cutter.shallowAlpha(alpha);
                final int shallowBeta = cutter.shallowBeta(beta);
                final int shallowDepth = cutter.shallowDepth;
                final int mpcScore = treeScore(mover, enemy, moverMoves, shallowAlpha, shallowBeta, shallowDepth, true);
                if (mpcScore >= shallowBeta) {
                    return beta;
                }
                if (mpcScore <= shallowAlpha) {
                    return alpha;
                }
                // todo test passing the best move to treeMove instead of having it do iid at shallowDepth > 1.
            }
        }
        return treeMove(mover, enemy, moverMoves, alpha, beta, depth, mpc).score;
    }

    private String indent(int depth) {
        final StringBuilder sb = new StringBuilder();
        for (int i = depth; i < rootDepth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

}
