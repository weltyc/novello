package com.welty.novello.solver;

import com.welty.novello.core.*;
import com.welty.novello.eval.Mpc;
import com.welty.novello.hash.MidgameHashTables;
import org.jetbrains.annotations.NotNull;

import static com.welty.novello.eval.CoefficientCalculator.DISK_VALUE;

/**
 * A reusable Search object.
 * <p/>
 * This class is not thread-safe.
 */
public class MidgameSearcher {
    public static final int SOLVER_START_DEPTH = 6;

    private final MidgameHashTables midgameHashTables = new MidgameHashTables();

    /**
     * Create with the given Counter and default options
     *
     * @param counter eval  + counter
     */
    public MidgameSearcher(@NotNull Counter counter) {
        this(counter, new Options(""));
    }

    /**
     * Create with the given counter and options
     *
     * @param counter eval + counter
     * @param options search options
     */
    public MidgameSearcher(@NotNull Counter counter, @NotNull Options options) {
        this.options = options;
        this.counter = counter;
    }

    /**
     * Create with the given counter and options
     *
     * @param counter eval + counter
     * @param options search options
     */
    public MidgameSearcher(Counter counter, String options) {
        this(counter, new Options(options));
    }

    /**
     * @return node counts (flips, evals) since the search was constructed.
     */
    @NotNull public Counts getCounts() {
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
    public MoveScore getMoveScore(Position position, long moverMoves, int depth) {
        this.rootDepth = depth;
        final BA ba = hashMove(position.mover(), position.enemy(), moverMoves, NovelloUtils.NO_MOVE, -NovelloUtils.NO_MOVE, depth);
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
        this.rootDepth = depth;
        return searchScore(mover, enemy, NovelloUtils.NO_MOVE, -NovelloUtils.NO_MOVE, depth);
    }

    private final @NotNull Options options;
    private final @NotNull Counter counter;

    /**
     * Depth of the search passed to move() or score() by the client.
     */
    private int rootDepth;

    public void clear() {
        midgameHashTables.clear(63);
    }

    static class BA {
        int bestMove = -1;
        int score = NovelloUtils.NO_MOVE;

        boolean isValid(int alpha) {
            return score <= alpha || bestMove >= 0;
        }
    }

    private static final long[] masks = {BitBoardUtils.CORNERS, ~(BitBoardUtils.CORNERS | BitBoardUtils.X_SQUARES), BitBoardUtils.X_SQUARES};

    /**
     * Find the best move in a position either from the hash table or mpc.
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
    BA hashMove(long mover, long enemy, long moverMoves, int alpha, int beta, int depth) {
        assert beta > alpha;
        assert depth > 0;

        // see if it cuts off
        final MidgameHashTables.Entry entry = midgameHashTables.find(mover, enemy);
        if (entry != null && entry.getDepth() >= depth) {
            if (entry.getMin() >= beta) {
                final BA ba = new BA();
                ba.bestMove = entry.getBestMove();
                ba.score = entry.getMin();
                return ba;
            }
            if (entry.getMax() <= alpha) {
                final BA ba = new BA();
                ba.bestMove = entry.getBestMove();
                ba.score = entry.getMax();
                return ba;
            }
        }

        final int suggestedMove = getSuggestedMove(mover, enemy, moverMoves, alpha, beta, depth);
        final BA ba = treeMoveWithPossibleSuggestion(mover, enemy, moverMoves, alpha, beta, depth, suggestedMove);
        midgameHashTables.store(mover, enemy, alpha, beta, depth, ba.bestMove, ba.score);

        assert ba.isValid(alpha);
        return ba;
    }

    private int getSuggestedMove(long mover, long enemy, long moverMoves, int alpha, int beta, int depth) {
        final MidgameHashTables.Entry entry = midgameHashTables.find(mover, enemy);

        if (entry != null && entry.getBestMove() >= 0) {
            return entry.getBestMove();
        } else if (depth > 2) {
            // internal iterative deepening
            return hashMove(mover, enemy, moverMoves, alpha, beta, depth > 3 ? 2 : 1).bestMove;
        }
        return -1;
    }

    private BA treeMoveWithPossibleSuggestion(long mover, long enemy, long moverMoves, int alpha, int beta, int depth, int suggestedMove) {
        final BA ba;
        if (suggestedMove >= 0) {
            ba = treeMoveWithSuggestion(mover, enemy, moverMoves, alpha, beta, depth, suggestedMove);
        } else {
            ba = treeMoveNoSuggestion(mover, enemy, moverMoves, alpha, beta, depth, new BA());
        }
        return ba;
    }

    /**
     * @return best move and score.
     */
    private BA treeMoveWithSuggestion(long mover, long enemy, long moverMoves, int alpha, int beta, int depth, int suggestedMove) {
        BA ba = new BA();
        final int subScore = calcMoveScore(mover, enemy, alpha, beta, depth, suggestedMove);
        if (subScore > ba.score) {
            ba.score = subScore;
            if (subScore > alpha) {
                ba.bestMove = suggestedMove;
                alpha = subScore;
                if (subScore >= beta) {
                    return ba;
                }
            }
        }
        moverMoves &= ~(1L << suggestedMove);
        return treeMoveNoSuggestion(mover, enemy, moverMoves, alpha, beta, depth, ba);
    }

    private BA treeMoveNoSuggestion(long mover, long enemy, long moverMoves, int alpha, int beta, int depth, BA ba) {
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

        return ba;
    }

    private int calcMoveScore(long mover, long enemy, int alpha, int beta, int depth, int sq) {
        final Square square = Square.of(sq);
        final long flips = counter.calcFlips(square, mover, enemy);
        final long subEnemy = mover | (1L << sq) | flips;
        final long subMover = enemy & ~flips;
        if (options.printSearch) {
            System.out.format("%s[%d] (%+5d,%+5d) scoring(%s):\n", indent(depth), depth, alpha, beta, BitBoardUtils.sqToText(sq));
        }
        final int subScore = -searchScore(subMover, subEnemy, -beta, -alpha, depth - 1);
        if (options.printSearch) {
            System.out.format("%s[%d] (%+5d,%+5d) score(%s)=%+5d\n", indent(depth), depth, alpha, beta, BitBoardUtils.sqToText(sq), subScore);
        }
        return subScore;
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
            final int score = treeScore(mover, enemy, moverMoves, alpha, beta, depth);
            return score;
        } else {
            final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
            if (enemyMoves != 0) {
                final int score = treeScore(enemy, mover, enemyMoves, -beta, -alpha, depth);
                return -score;
            } else {
                return DISK_VALUE * BitBoardUtils.terminalScore(mover, enemy);
            }
        }
    }

    int treeScore(long mover, long enemy, long moverMoves, int alpha, int beta, int depth) {
        if (depth <= 0) {
            return counter.eval(mover, enemy);
        }

        final int nEmpty = BitBoardUtils.nEmpty(mover, enemy);
        if (options.useSolver && nEmpty <= SOLVER_START_DEPTH) {
            final int solverAlpha = solverAlpha(alpha);
            final int solverBeta = solverBeta(beta);
            return ShallowSolver.solveNoParity(counter, mover, enemy, solverAlpha, solverBeta, nEmpty, moverMoves) * DISK_VALUE;
        }

        if (options.mpc && depth >= 2) {
            return mpcMove(mover, enemy, moverMoves, alpha, beta, depth).score;
        }
        return hashMove(mover, enemy, moverMoves, alpha, beta, depth).score;
    }

    static int solverBeta(int beta) {
        assert beta >= -64 * DISK_VALUE;

        if (beta > 64 * DISK_VALUE) {
            return 64;
        }
        return (65 * DISK_VALUE + beta - 1) / DISK_VALUE - 64;
    }

    static int solverAlpha(int alpha) {
        assert alpha <= 64 * DISK_VALUE;

        if (alpha < -64 * DISK_VALUE) {
            return -64;
        }
        return (65 * DISK_VALUE + alpha) / DISK_VALUE - 65;
    }

    /**
     * Search using MPC. Return score and, if available, best move.
     * <p/>
     * Like other routines, this will return -1 if the best move was not available due to alpha cutoff.
     * Unlike other routines, it can also return -1 if score >= beta due to a depth-0 cutoff .
     *
     * @return best move (or -1 if no best move) and score
     */
    private BA mpcMove(long mover, long enemy, long moverMoves, int alpha, int beta, int depth) {
        final BA ba = new BA();

        // see if it cuts off
        final MidgameHashTables.Entry entry = midgameHashTables.find(mover, enemy);
        if (entry != null && entry.getDepth() >= depth) {
            if (entry.getMin() >= beta) {
                ba.bestMove = entry.getBestMove();
                ba.score = entry.getMin();
                return ba;
            }
            if (entry.getMax() <= alpha) {
                ba.bestMove = entry.getBestMove();
                ba.score = entry.getMax();
                return ba;
            }
        }

        final int nEmpty = BitBoardUtils.nEmpty(mover, enemy);
        Mpc.Cutter[] cutters = counter.mpcs.cutters(nEmpty, depth);

        for (Mpc.Cutter cutter : cutters) {
            final int shallowAlpha = cutter.shallowAlpha(alpha);
            final int shallowBeta = cutter.shallowBeta(beta);
            final int shallowDepth = cutter.shallowDepth;
            if (shallowDepth <= 0) {
                final int mpcScore = counter.eval(mover, enemy);
                if (mpcScore >= shallowBeta) {
                    ba.score = beta;
                    return ba;
                }
                if (mpcScore <= shallowAlpha) {
                    ba.score = alpha;
                    return ba;
                }
            } else {
                BA mpcBa = mpcMove(mover, enemy, moverMoves, shallowAlpha, shallowBeta, shallowDepth);
                if (mpcBa.score >= shallowBeta) {
                    ba.score = beta;
                    ba.bestMove = mpcBa.bestMove;
                    return ba;
                }
                if (mpcBa.score <= shallowAlpha) {
                    ba.score = alpha;
                    return ba;
                }
                ba.bestMove = mpcBa.bestMove;
            }
        }

        final int suggestedMove = getSuggestedMove(mover, enemy, moverMoves, alpha, beta, depth);
        final BA ba1 = treeMoveWithPossibleSuggestion(mover, enemy, moverMoves, alpha, beta, depth, suggestedMove);
        midgameHashTables.store(mover, enemy, alpha, beta, depth, ba1.bestMove, ba1.score);

        assert ba1.isValid(alpha);
        return ba1;
    }

    private String indent(int depth) {
        final StringBuilder sb = new StringBuilder();
        for (int i = depth; i < rootDepth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    public static class Options {
        final boolean mpc;
        final boolean useSolver;
        final boolean printSearch;

        public Options(String options) {
            mpc = !options.contains("w");
            useSolver = options.contains("s");
            printSearch = options.contains("p");
        }
    }
}
