/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.solver;

import com.welty.novello.book.Book;
import com.welty.novello.core.*;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Eval;
import com.welty.novello.hash.Entry;
import com.welty.novello.hash.HashTables;
import com.welty.novello.selfplay.Players;
import com.welty.novello.external.api.AbortCheck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.Long.bitCount;

/**
 * A Solver solves positions. It can run in only one thread at a time.
 */
public class Solver {

    /**
     * At this depth and above, the search will check in the hash table,
     * but only if move sorting is enabled.
     */
    public static final int MIN_HASH_DEPTH = 6;

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

    /**
     * Abort check, to see whether the search should be aborted.
     * <p/>
     * This must be set at the start of every search.
     */
    private @NotNull AbortCheck abortCheck;

    private final @NotNull Counter counter;
    public final @NotNull MidgameSearcher midgameSearcher;
    @Nullable private final Book book;

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
    private StatsListener statsListener;
    private int minBookCheckEmpties;

    /**
     * Set up data structures for a Solver.
     * <p/>
     * This Solver can be reused to solve multiple positions, but may only run in one thread at a time.
     */
    public Solver() {
        this(Players.currentEval());
    }

    public Solver(@NotNull Eval eval) {
        this(new Counter(eval), new MidgameSearcher.Options(""), null);
    }

    public Solver(@NotNull Eval eval, MidgameSearcher.Options options, @Nullable Book book) {
        this(new Counter(eval), options, book);
    }

    private Solver(Counter counter, MidgameSearcher.Options options, Book book) {
        this(counter, new MidgameSearcher(counter, options, book), book);
    }

    private Solver(@NotNull Counter counter, @NotNull MidgameSearcher midgameSearcher, @Nullable Book book) {
        this.counter = counter;
        this.midgameSearcher = midgameSearcher;
        this.book = book;
        treeSearchResults = new TreeSearchResult[64];
        for (int i = 0; i < treeSearchResults.length; i++) {
            treeSearchResults[i] = new TreeSearchResult();
        }
        moveSorters = new MoveSorters(counter, midgameSearcher);
    }

    /**
     * @param mover bitboard of mover's disks
     * @param enemy bitboard of enemy's disks
     * @return value of the game to the mover
     */
    public int solve(long mover, long enemy) {
        try {
            return solve(mover, enemy, AbortCheck.NEVER, StatsListener.NULL);
        } catch (SearchAbortedException e) {
            // this can never happen because we used AbortCheck.NEVER
            throw new IllegalStateException("Shouldn't be here.");
        }
    }

    /**
     * @param mover bitboard of mover's disks
     * @param enemy bitboard of enemy's disks
     * @return value of the game to the mover, in disks
     */
    public int solve(long mover, long enemy, @NotNull AbortCheck abortCheck, @NotNull StatsListener statsListener) throws SearchAbortedException {
        this.statsListener = statsListener;
        this.empties = ShallowSolver.createEmptiesList(mover, enemy);
        this.abortCheck = abortCheck;
        setMinBookCheckEmpties(mover, enemy);

        return solve(mover, enemy, -64, 64);
    }

    public MoveScore calcSubMoveScore(int sq, Board pos, int alpha, int beta, AbortCheck abortCheck, StatsListener statsListener) throws SearchAbortedException {
        Board subPos = pos.play(sq);
        String pv = BitBoardUtils.sqToLowerText(sq);

        if (subPos.hasLegalMove()) {
            final MoveScore subMoveScore = getMoveScore(subPos.mover(), subPos.enemy(), abortCheck, statsListener, -beta, -alpha);
            if (subMoveScore.pv != null) {
                pv += "-" + subMoveScore.pv;
            }
            return new MoveScore(sq, -subMoveScore.centidisks, pv);
        }
        else {
            subPos = subPos.pass();
            if (subPos.hasLegalMove()) {
                final MoveScore subMoveScore = getMoveScore(subPos.mover(), subPos.enemy(), abortCheck, statsListener, alpha, beta);
                if (subMoveScore.pv != null) {
                    pv += "-pa-" + subMoveScore.pv;
                }
                return new MoveScore(sq, subMoveScore.centidisks, pv);
            }
            else {
                return new MoveScore(sq, subPos.terminalScoreToBlack() * CoefficientCalculator.DISK_VALUE, pv);
            }
        }
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
    public @NotNull MoveScore getMoveScore(long mover, long enemy) {
        try {
            return getMoveScore(mover, enemy, AbortCheck.NEVER, StatsListener.NULL);
        } catch (SearchAbortedException e) {
            // this can never happen because we used AbortCheck.NEVER
            throw new IllegalStateException("Shouldn't be here.");
        }
    }

    /**
     * Solve the game and return the best move.
     * <p/>
     * Precondition: mover has a legal move
     *
     * @param mover      mover disks
     * @param enemy      enemy disks
     * @param abortCheck checker for whether search should abort
     * @return a {@link MoveScore} containing the best move
     * @throws SearchAbortedException if abortCheck returned true
     */
    public @NotNull MoveScore getMoveScore(long mover, long enemy, @NotNull AbortCheck abortCheck, @NotNull StatsListener statsListener) throws SearchAbortedException {
        return getMoveScore(mover, enemy, abortCheck, statsListener, -64, 64);
    }

    public MoveScore getMoveScore(long mover, long enemy, AbortCheck abortCheck, StatsListener statsListener, int alpha, int beta) throws SearchAbortedException {
        if (BitBoardUtils.calcMoves(mover, enemy) == 0) {
            throw new IllegalArgumentException("mover must have a legal move");
        }
        this.empties = ShallowSolver.createEmptiesList(mover, enemy);
        this.abortCheck = abortCheck;
        this.statsListener = statsListener;
        setMinBookCheckEmpties(mover, enemy);

        final int nEmpties = bitCount(~(mover | enemy));
        final TreeSearchResult result = treeSearchResults[nEmpties];

        final long parity = empties.calcParity();
        moverResultWithSorting(result, mover, enemy, alpha, beta, nEmpties, parity, PRED_PV, -1);
        final MoveSorter moveSorter = moveSorters.get(nEmpties);
        final int sq = moveSorter.sq(result.iBestMove);
        return new MoveScore(sq, result.score * CoefficientCalculator.DISK_VALUE);
    }

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
    private int solve(long mover, long enemy, int alpha, int beta) throws SearchAbortedException {
        final int nEmpty = bitCount(~(mover | enemy));
        if (nEmpty == 0) {
            return BitBoardUtils.terminalScore(mover, enemy);
        }
        if (nEmpty == 1) {
            return ShallowSolver.solve1(counter, mover, enemy, empties.first().square);
        }
        if (nEmpty == 2) {
            final ListOfEmpties.Node first = empties.first();
            return ShallowSolver.solve2(counter, mover, enemy, alpha, beta, first.square, first.next.square);
        }
        if (nEmpty < ShallowSolver.MIN_PARITY_DEPTH) {
            return ShallowSolver.solveNoParity(counter, mover, enemy, alpha, beta, empties, nEmpty);
        }
        return solveDeep(mover, enemy, alpha, beta, nEmpty, empties.calcParity(), PRED_PV, -1L);
    }

    private void setMinBookCheckEmpties(long mover, long enemy) {
        minBookCheckEmpties = BitBoardUtils.nEmpty(mover, enemy)-3;
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
            , long movesToCheck) throws SearchAbortedException {
        if (nEmpties >= 14) {
            if (abortCheck.shouldAbort()) {
                throw new SearchAbortedException();
            }
            if (nEmpties >= 21) {
                statsListener.update();
            }
            if (book!=null && BitBoardUtils.nEmpty(mover, enemy) >= minBookCheckEmpties) {
                final Board board = new Board(mover, enemy, true);
                final Book.Data data = book.getData(board);
                // make sure we only use SOLVED book data when solving!
                if (data!=null && data.getNodeType()== Book.NodeType.SOLVED) {
                    return data.getScore();
                }
            }
        }
        if (nEmpties < ShallowSolver.MIN_PARITY_DEPTH) {
            return ShallowSolver.solveNoParity(counter, mover, enemy, alpha, beta, empties, nEmpties);
        }
        nodeCounts.update(nEmpties, nodeType);
        final int result = moverResultDeep(mover, enemy, alpha, beta, nEmpties, parity, nodeType, movesToCheck);
        if (result == ShallowSolver.NO_MOVE) {
            final int enemyResult = moverResultDeep(enemy, mover, -beta, -alpha, nEmpties, parity, -nodeType, -1L);
            if (enemyResult == ShallowSolver.NO_MOVE) {
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
            , int nodeType, long movesToCheck) throws SearchAbortedException {
        if (nEmpties < MIN_SORT_DEPTH) {
            return ShallowSolver.moverResultNoSort(counter, mover, enemy, alpha, beta, empties, nEmpties, parity, movesToCheck);
        } else {
            return moverResultWithHash(mover, enemy, alpha, beta, nEmpties, parity, nodeType, movesToCheck);
        }
    }

    private int moverResultWithHash(long mover, long enemy, int alpha, int beta, int nEmpties, long parity, int nodeType, long movesToCheck) throws SearchAbortedException {
        // searchAlpha and searchBeta are the alpha and beta used for the search.
        // They are normally equal to the original alpha and beta.
        //
        // If there is a hash hit, that doesn't cut off, though, the hash information will be used to update
        // searchAlpha and searchBeta. When this happens we need to save the original alpha and beta
        // so we can correctly update the hash entry at the end of the search.
        int searchAlpha = alpha;
        int searchBeta = beta;
        if (nEmpties >= MIN_HASH_DEPTH) {
            Entry entry = hashTables.getEntry(mover, enemy);
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized(entry) {
                if (entry.matches(mover, enemy)) {
                    int min = entry.getMin();
                    if (min >= beta) {
                        hashTables.updateBetaCut();
                        return min;
                    }
                    int max = entry.getMax();
                    if (max <= alpha) {
                        hashTables.updateAlphaCut();
                        return max;
                    }
                    if (min == max) {
                        hashTables.updatePvCut();
                        return min;
                    }
                    if (min > searchAlpha) {
                        searchAlpha = min;
                    }
                    if (max < searchBeta) {
                        searchBeta = max;
                    }
                    hashTables.updateUselessFind();
                }
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
            , int nEmpties, long parity, int nodeType, long movesToCheck) throws SearchAbortedException {
        int score = ShallowSolver.NO_MOVE;
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
                        assert iBestMove >= 0 || score == ShallowSolver.NO_MOVE;
                        return;
                    }
                    alpha = subResult;
                }
            }
            subNodeType = PRED_CUT;
        }
        treeSearchResult.score = score;
        treeSearchResult.iBestMove = iBestMove;
        assert iBestMove >= 0 || score == ShallowSolver.NO_MOVE;
    }

    void dumpStatistics() {
        System.out.println(cutoffStatistics);
        System.out.println(stableStatistics);
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
