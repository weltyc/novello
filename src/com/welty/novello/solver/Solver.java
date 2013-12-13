package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Counts;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Square;
import com.welty.novello.selfplay.Players;
import org.jetbrains.annotations.NotNull;

import static java.lang.Long.bitCount;

/**
 * A Solver solves positions. It can run in only one thread at a time.
 */
public class Solver {
    /**
     * At this depth and above, the search will check moves into odd parity regions
     * before moves into even parity regions
     */
    private static final int MIN_PARITY_DEPTH = 5;

    /**
     * At this depth and above, the search will check in the hash table,
     * but only if move sorting is enabled.
     */
    static final int MIN_HASH_DEPTH = 6;

    /**
     * At this depth and above, the search will use NEGASCOUT,
     * but only when move sorting is enabled.
     */
    private static final int MIN_NEGASCOUT_DEPTH = 10;

    /**
     * At this depth and above, the search will do a full sort of the remaining moves
     */
    static int MIN_SORT_DEPTH = 6;

    /**
     * A MoveSorter is created for each search tree depth.
     * <p/>
     * These MoveSorters are shared by each node at a given depth.
     * This avoids the overhead of creating a MoveSorter at each node.
     * <p/>
     * These are now member variables to allow multiple Threads to each run a Solver.
     */
    private final MoveSorters moveSorters;

    /**
     * A TreeSearchResult is created for each search tree depth.
     * <p/>
     * These MoveResults are shared by each node at a given depth.
     * This avoids the overhead of creating a TreeSearchResult at each node.
     */
    private final TreeSearchResult[] treeSearchResults;

    /**
     * List of empty squares, in a static search order.
     * <p/>
     * Squares are removed/replaced during the course of the search.
     */
    private ListOfEmpties empties;

    private final Counter counter = new Counter(Players.currentEval());

    /**
     * Statistics on nodes, cutoffs, etc.
     */
    private final NodeCounts nodeCounts = new NodeCounts();
    private final CutoffStatistics cutoffStatistics = new CutoffStatistics();
    private final StableStatistics stableStatistics = new StableStatistics();

    /**
     * Transposition table.
     */
    final HashTables hashTables = new HashTables();

    /**
     * Set up data structures for a Solver.
     * <p/>
     * This Solver can be reused to solve multiple positions, but may only run in one thread at a time.
     */
    public Solver() {
        treeSearchResults = new TreeSearchResult[64];
        for (int i = 0; i < treeSearchResults.length; i++) {
            treeSearchResults[i] = new TreeSearchResult();
        }
        moveSorters = new MoveSorters(counter);
    }

    /**
     * @param mover bitboard of mover's disks
     * @param enemy bitboard of enemy's disks
     * @return value of the game to the mover
     */
    public int solve(long mover, long enemy) {
        this.empties = createEmptiesList(mover, enemy);

        return solve(mover, enemy, -64, 64);
    }

    /**
     * Solve the game and return the best move.
     * <p/>
     * Precondition: mover has a legal move
     *
     * @param mover mover disks
     * @param enemy enemy disks
     * @return a {@link MoveScore} containing the best move
     */
    public MoveScore solveWithMove(long mover, long enemy) {
        if (BitBoardUtils.calcMoves(mover, enemy) == 0) {
            throw new IllegalArgumentException("mover must have a legal move");
        }
        this.empties = createEmptiesList(mover, enemy);
        final int nEmpties = bitCount(~(mover | enemy));
        final TreeSearchResult result = treeSearchResults[nEmpties];

        final long parity = calcParity();
        moverResultWithSorting(result, mover, enemy, -64, 64, nEmpties, parity, PRED_PV, -1);
        final MoveSorter moveSorter = moveSorters.get(nEmpties);
        final int sq = moveSorter.sq(result.iBestMove);
        return new MoveScore(sq, result.score);
    }

    private static ListOfEmpties createEmptiesList(long mover, long enemy) {
        long empties = ~(mover | enemy);
        final ListOfEmpties emptySquares = new ListOfEmpties();
        for (long mask : FixedMoveOrdering.masks) {
            populateUnsorted(emptySquares, empties & mask);
        }
        return emptySquares;
    }

    private static void populateUnsorted(ListOfEmpties emptySquares, long empties) {
        while (empties != 0) {
            final int sq = Long.numberOfTrailingZeros(empties);
            final Square square = Square.of(sq);
            empties ^= square.placement();
            emptySquares.add(square);
        }
    }

    private static final int NO_MOVE = -65;

    /**
     * Solve the position with fail-soft alpha-beta.
     * <p/>
     * Precondition: 64 >= beta > alpha >= -64.
     * <p/>
     * If the perfect-play value is pp, this function's return value 'solve' satisfies:
     * <ul>
     * <li>pp >= solve >= beta if pp>= beta</li>
     * <li>solve = pp if beta >= pp >= alpha</li>
     * <li>alpha >= solve >= pp if alpha >= pp</li>
     * </ul>
     *
     * @param mover mover disks bitboard
     * @param enemy enemy disks bitboard
     * @param alpha alpha for alpha-beta search
     * @param beta  beta for alpha-beta search
     * @return net disks to mover, with perfect play. Winner does NOT get empties.
     */
    private int solve(long mover, long enemy, int alpha, int beta) {
        final int nEmpty = bitCount(~(mover | enemy));
        if (nEmpty == 0) {
            return BitBoardUtils.terminalScore(mover, enemy);
        }
        if (nEmpty == 1) {
            return solve1(mover, enemy, empties.first().square);
        }
        if (nEmpty == 2) {
            final ListOfEmpties.Node first = empties.first();
            return solve2(mover, enemy, alpha, beta, first.square, first.next.square);
        }
        if (nEmpty < MIN_PARITY_DEPTH) {
            return solveNoParity(mover, enemy, alpha, beta, empties, nEmpty);
        }
        return solveDeep(mover, enemy, alpha, beta, nEmpty, calcParity(), PRED_PV, -1L);
    }

    private long calcParity() {
        long parity = 0;
        for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
            parity ^= node.square.parityRegion;
        }
        return parity;
    }

    /*
     * Predicted node types
     */
    static final int PRED_CUT = 1;
    static final int PRED_PV = 0;
    static final int PRED_ALL = -1;

    /**
     * The nodeType is a hint to this routine. It describes the expected return value of the routine.
     * <p/>
     * If the node type is PRED_ALL, we expect the value to be below alpha. The routine may spend less effort
     * on move ordering in this case.
     * <p/>
     * If the node type is PRED_PV, we expect the value to be between alpha and beta.
     * <p/>
     * If the node type is PRED_CUT, we expect the value to be above beta.
     *
     * @param nodeType     one of PRED_CUT, PRED_PV, or PRED_ALL
     * @param movesToCheck bitBoard containing moves to check (if the mover moves).
     */
    private int solveDeep(long mover, long enemy, int alpha, int beta, int nEmpties, long parity, int nodeType
            , long movesToCheck) {
        if (nEmpties < MIN_PARITY_DEPTH) {
            return solveNoParity(mover, enemy, alpha, beta, empties, nEmpties);
        }
        nodeCounts.update(nEmpties, nodeType);
        final int result = moverResultDeep(mover, enemy, alpha, beta, nEmpties, parity, nodeType, movesToCheck);
        if (result == NO_MOVE) {
            final int enemyResult = moverResultDeep(enemy, mover, -beta, -alpha, nEmpties, parity, -nodeType, -1L);
            if (enemyResult == NO_MOVE) {
                return BitBoardUtils.terminalScore(mover, enemy);
            } else {
                return -enemyResult;
            }
        } else {
            return result;
        }
    }

    /**
     * alpha, beta, result from mover's point of view.
     *
     * @return solve value according to fail-soft alpha/beta, unless there are no legal moves in which case it returns NO_MOVE.
     */
    private int moverResultDeep(long mover, long enemy, int alpha, int beta, int nEmpties, long parity
            , int nodeType, long movesToCheck) {
        if (nEmpties < MIN_SORT_DEPTH) {
            return moverResultNoSort(mover, enemy, alpha, beta, nEmpties, parity, nodeType, movesToCheck);
        } else {
            return moverResultWithHash(mover, enemy, alpha, beta, nEmpties, parity, nodeType, movesToCheck);
        }
    }

    private int moverResultNoSort(long mover, long enemy, int alpha, int beta, int nEmpties, long parity, int nodeType, long movesToCheck) {
        int result = NO_MOVE;
        int subNodeType = -nodeType;

        // sort by parity only
        for (int desiredParity = 1; desiredParity >= 0; desiredParity--) {
            for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
                // parity nodes first
                final Square square = node.square;
                if (BitBoardUtils.isBitClear(movesToCheck, square.sq)) {
                    continue;
                }
                if (BitBoardUtils.getBit(parity, square.sq) == desiredParity) {
                    final long flips = counter.calcFlips(square, mover, enemy);
                    if (flips != 0) {
                        final long subMover = enemy & ~flips;
                        final long subEnemy = mover | flips | square.placement();
                        node.remove();
                        final int subResult = -solveDeep(subMover, subEnemy, -beta, -alpha, nEmpties - 1
                                , parity ^ square.parityRegion, subNodeType, -1L);
                        node.restore();
                        if (subResult > result) {
                            result = subResult;
                            if (subResult > alpha) {
                                if (subResult >= beta) {
                                    return result;
                                }
                                alpha = subResult;
                            }
                        }
                        subNodeType = PRED_CUT;
                    }
                }
            }
        }
        return result;
    }

    private int moverResultWithHash(long mover, long enemy, int alpha, int beta, int nEmpties, long parity, int nodeType, long movesToCheck) {
        // searchAlpha and searchBeta are the alpha and beta used for the search.
        // They are normally equal to the original alpha and beta.
        //
        // If there is a hash hit, that doesn't cut off, though, the hash information will be used to update
        // searchAlpha and searchBeta. When this happens we need to save the original alpha and beta
        // so we can correctly update the hash entry at the end of the search.
        int searchAlpha = alpha;
        int searchBeta = beta;
        if (nEmpties >= MIN_HASH_DEPTH) {
            HashTables.Entry entry = hashTables.find(mover, enemy);
            if (entry != null) {
                if (entry.min >= beta) {
                    hashTables.nBetaCuts++;
                    return entry.min;
                }
                if (entry.max <= alpha) {
                    hashTables.nAlphaCuts++;
                    return entry.max;
                }
                if (entry.min == entry.max) {
                    hashTables.nPvCuts++;
                    return entry.min;
                }
                if (entry.min > searchAlpha) {
                    searchAlpha = entry.min;
                }
                if (entry.max < searchBeta) {
                    searchBeta = entry.max;
                }
                hashTables.nUselessFind++;
            }
        }
        if (nEmpties == 6) {
            // only do this expensive calculation if it has a chance of working
            // Estimate total stable disks at 2*edge stable disks and see if it cuts off -
            // if it would, do the full stability calculation.
            final long edgeStable = Stable.edgeStable(mover, enemy);
            final int enemyEdgeStables = Long.bitCount(enemy & edgeStable);
            final int moverEdgeStables = Long.bitCount(mover & edgeStable);
            final int estimatedUpperBound = 64 - 4 * enemyEdgeStables;
            final int estimatedLowerBound = 4 * moverEdgeStables - 64;

            if (estimatedLowerBound >= beta || estimatedUpperBound <= alpha) {
                // A lower bound on the score is 2*mover stables - 64.
                // an upper bound on the score is 64-2*enemy stables
                final long stable = Stable.stable(mover, enemy);
                final int enemyStable = Long.bitCount(stable & enemy);
                final int scoreUpperBound = 64 - 2 * enemyStable;
                final int moverStable = Long.bitCount(stable & mover);
                final int scoreLowerBound = 2 * moverStable - 64;
                stableStatistics.counts[Math.max(enemyStable, moverStable)]++;
                if (scoreUpperBound <= alpha) {
                    stableStatistics.alphaCuts++;
                    return scoreUpperBound;
                }
                if (scoreLowerBound >= beta) {
                    stableStatistics.betaCuts++;
                    return scoreLowerBound;
                }
                stableStatistics.fails++;
            } else {
                stableStatistics.uncalculated++;
            }
        }
        final TreeSearchResult result = treeSearchResults[nEmpties];
        moverResultWithSorting(result, mover, enemy, searchAlpha, searchBeta, nEmpties, parity, nodeType
                , movesToCheck);
        if (nEmpties >= MIN_HASH_DEPTH) {
            hashTables.store(mover, enemy, alpha, beta, result.score);
        }
        return result.score;
    }

    /**
     * Fills in treeSearchResult with the score.
     * Sets the treeSearchResult.iBestMove to the index of the highest scoring move in sorter.moves.
     * If the highest scoring move scored < alpha then this is kind of arbitrary.
     * <p/>
     * If there is no legal move, returns score==NO_MOVE and iBestMove==-1.
     */
    private void moverResultWithSorting(TreeSearchResult treeSearchResult, long mover, long enemy, int alpha, int beta
            , int nEmpties, long parity, int nodeType, long movesToCheck) {
        int score = NO_MOVE;
        int iBestMove = -1;

        int subNodeType = -nodeType;
        // do an actual move sort
        final MoveSorter sorter = moveSorters.get(nEmpties);
        sorter.createSort(mover, enemy, alpha, beta, nEmpties, parity, movesToCheck, this.empties, this.hashTables, nodeType);
        final int n = sorter.size();
        for (int i = 0; i < n; i++) {
            final SorterMove sorterMove = sorter.sorterMoves[i];
            final long subMover = enemy & ~sorterMove.flips;
            final Square square = sorterMove.node.square;
            final long subEnemy = mover | sorterMove.flips | square.placement();
            sorterMove.node.remove();
            int subResult;
            if (i > 0 && nEmpties >= MIN_NEGASCOUT_DEPTH) {
                // use Negascout for nodes after the first. The thought is that nodes after the first will have values lower than
                // the value of the first node; thus we can save nodes by searching in a window (alpha, alpha+1).
                // If we were correct, this value < alpha and we saved some nodes. If we were wrong, this value >= alpha
                // and we need to re-search the position at full width.
                subResult = -solveDeep(subMover, subEnemy, -alpha - 1, -alpha, nEmpties - 1
                        , parity ^ square.parityRegion, subNodeType, sorterMove.enemyMoves);

                // Re-search if the score ended up between alpha and beta.
                // This condition is always false if we were already in a Negascout search, because beta = alpha + 1
                // So this won't search twice in a Negascout search.
                //
                // This re-search can't be a CUT node because it can't fail low. We'll predict ALL.
                if (subResult > alpha && subResult < beta) {
                    subResult = -solveDeep(subMover, subEnemy, -beta, -subResult, nEmpties - 1
                            , parity ^ square.parityRegion, PRED_ALL, sorterMove.enemyMoves);
                }
            } else {
                subResult = -solveDeep(subMover, subEnemy, -beta, -alpha, nEmpties - 1
                        , parity ^ square.parityRegion, subNodeType, sorterMove.enemyMoves);
            }
            sorterMove.node.restore();
//            // todo remove once statistics are collected
//            if (nEmpties >= MIN_EVAL_SORT_DEPTH) {
//                collectStatistics(subMover, subEnemy, -beta, -alpha, -subResult, subNodeType);
//            }

            if (subResult > score) {
                score = subResult;
                iBestMove = i;
                if (subResult > alpha) {
                    if (subResult >= beta) {
                        treeSearchResult.score = score;
                        treeSearchResult.iBestMove = iBestMove;
                        nodeCounts.updateCut(nEmpties, i);
                        assert iBestMove >= 0 || score == NO_MOVE;
                        return;
                    }
                    alpha = subResult;
                }
            }
            subNodeType = PRED_CUT;
        }
        treeSearchResult.score = score;
        treeSearchResult.iBestMove = iBestMove;
        assert iBestMove >= 0 || score == NO_MOVE;
    }

    void dumpStatistics() {
        System.out.println(cutoffStatistics);
        System.out.println(stableStatistics);
    }


    private int solveNoParity(long mover, long enemy, int alpha, int beta, ListOfEmpties empties, int nEmpties) {
        if (nEmpties == 3) {
            return solve3(mover, enemy, alpha, beta, empties);
        }
        nodeCounts.update(nEmpties);
        final int result = moverResultNoParity(empties, mover, enemy, alpha, beta, nEmpties);
        if (result == NO_MOVE) {
            final int enemyResult = moverResultNoParity(empties, enemy, mover, -beta, -alpha, nEmpties);
            if (enemyResult == NO_MOVE) {
                return BitBoardUtils.terminalScore(mover, enemy);
            } else {
                return -enemyResult;
            }
        } else {
            return result;
        }
    }

    /**
     * alpha, beta, result from mover's point of view.
     *
     * @return solve value according to fail-soft alpha/beta, unless there are no legal moves in which case it returns NO_MOVE.
     */
    private int moverResultNoParity(ListOfEmpties empties, long mover, long enemy, int alpha, int beta, int nEmpties) {
        int result = NO_MOVE;
        for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
            final Square square = node.square;
            final long flips = counter.calcFlips(square, mover, enemy);
            if (flips != 0) {
                final long subMover = enemy & ~flips;
                final long subEnemy = mover | flips | square.placement();
                node.remove();
                final int subResult = -solveNoParity(subMover, subEnemy, -beta, -alpha, empties, nEmpties - 1);
                node.restore();
                if (subResult > result) {
                    result = subResult;
                    if (subResult > alpha) {
                        if (subResult >= beta) {
                            return result;
                        }
                        alpha = subResult;
                    }
                }
            }
        }
        return result;
    }

    private int solve3(long mover, long enemy, int alpha, int beta, ListOfEmpties empties) {
        nodeCounts.update(3);
        final int result = moverResult3(empties, mover, enemy, alpha, beta);
        if (result == NO_MOVE) {
            final int enemyResult = moverResult3(empties, enemy, mover, -beta, -alpha);
            if (enemyResult == NO_MOVE) {
                return BitBoardUtils.terminalScore(mover, enemy);
            } else {
                return -enemyResult;
            }
        } else {
            return result;
        }
    }

    /**
     * alpha, beta, result from mover's point of view.
     *
     * @return solve value according to fail-soft alpha/beta, unless there are no legal moves in which case it returns NO_MOVE.
     */
    private int moverResult3(ListOfEmpties empties, long mover, long enemy, int alpha, int beta) {
        int result = NO_MOVE;
        for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
            final Square square = node.square;
            final long flips = counter.calcFlips(square, mover, enemy);
            if (flips != 0) {
                final long subMover = enemy & ~flips;
                final long subEnemy = mover | flips | square.placement();
                node.remove();
                final int subResult = -solve2(subMover, subEnemy, -beta, -alpha, empties.first().square, empties.first().next.square);
                node.restore();
                if (subResult > result) {
                    result = subResult;
                    if (subResult > alpha) {
                        if (subResult >= beta) {
                            return result;
                        }
                        alpha = subResult;
                    }
                }
            }
        }
        return result;
    }

    private int solve2(long mover, long enemy, int alpha, int beta, Square empty1, Square empty2) {
        nodeCounts.update(2);
        final int result = moverResult2(mover, enemy, beta, empty1, empty2);
        if (result == NO_MOVE) {
            final int enemyResult = moverResult2(enemy, mover, -alpha, empty1, empty2);
            if (enemyResult == NO_MOVE) {
                return BitBoardUtils.terminalScore(mover, enemy);
            } else {
                return -enemyResult;
            }
        } else {
            return result;
        }
    }

    /**
     * alpha, beta, result from mover's point of view.
     *
     * @return solve value according to fail-soft alpha/beta, unless there are no legal moves in which case it returns NO_MOVE.
     */
    private int moverResult2(long mover, long enemy, int beta, Square empty1, Square empty2) {
        int result = NO_MOVE;
        final long flips1 = counter.calcFlips(empty1, mover, enemy);
        if (flips1 != 0) {
            final long subMover = enemy & ~flips1;
            final long subEnemy = mover | flips1 | empty1.placement();
            final int subResult = -solve1(subMover, subEnemy, empty2);
            if (subResult > result) {
                result = subResult;
                if (subResult >= beta) {
                    return result;
                }
            }
        }

        final long flips2 = counter.calcFlips(empty2, mover, enemy);
        if (flips2 != 0) {
            final long subMover = enemy & ~flips2;
            final long subEnemy = mover | flips2 | empty2.placement();
            final int subResult = -solve1(subMover, subEnemy, empty1);
            if (subResult > result) {
                result = subResult;
            }
        }

        return result;
    }


    /**
     * Solve when there is only 1 empty square.
     *
     * @param mover mover bitboard
     * @param enemy enemy bitboard
     * @param empty the empty square
     * @return solve value, exact.
     */
    private int solve1(long mover, long enemy, Square empty) {
        nodeCounts.update(1);

        final long moverFlips = counter.calcFlips(empty, mover, enemy);
        if (moverFlips != 0) {
            mover |= moverFlips;
            final int net = 2 * bitCount(mover) - 62; // -62 because we didn't set the placed disk
            return net;
        }
        final long enemyFlips = counter.calcFlips(empty, enemy, mover);
        if (enemyFlips != 0) {
            enemy |= enemyFlips;
            final int net = 62 - 2 * bitCount(enemy); // 62 because we didn't set the placed disk
            return net;
        }
        int net = 2 * bitCount(mover) - 63; // 63 because 1 empty square remains
        if (BitBoardUtils.WINNER_GETS_EMPTIES) {
            if (net > 0) {
                net++;
            } else {
                // net can't be 0 because 1 empty remains, so must be negative
                net--;
            }
        }
        return net;
    }

    /**
     * Clear all historical information so we don't cheat while benchmarking.
     * <p/>
     * For speed, only clear up to maxNEmpties.
     *
     * @param maxNEmpties max # of empties that will be cleared in the HashTables
     */
    public void clear(int maxNEmpties) {
        hashTables.clear(maxNEmpties);
    }

    public @NotNull Counts getCounts() {
        return counter.getNodeStats();
    }

    public String getNodeCountsByDepth() {
        return nodeCounts.getNodeCountsByDepth();
    }


    /**
     * A class which holds the results of a tree search.
     * <p/>
     * The best move is returned so that the hash table can be updated. -1 is returned if no move was found with value >= alpha.
     */
    private static class TreeSearchResult {
        int score;

        /**
         * Index of best move in the search. For instance the first move searched is 0, the second move searched is 1, etc.
         * The 'best move' is the last move that increased alpha, or -1 if no move increased alpha.
         */
        int iBestMove;
    }

}
