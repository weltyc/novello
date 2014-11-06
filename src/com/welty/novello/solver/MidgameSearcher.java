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
import com.welty.novello.hash.MidgameHashTables;
import com.welty.othello.api.AbortCheck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ForkJoinPool;

/**
 * A reusable Search object.
 * <p/>
 * This class is not thread-safe.
 */
public class MidgameSearcher {
    public static final int SOLVER_START_DEPTH = 6;

    private final MidgameHashTables midgameHashTables = new MidgameHashTables();

    private final @NotNull Options options;
    private final @NotNull Counter counter;
    @Nullable private final Book book;

    //////////////////////////////////////////////////////////
    //
    // Mutable options, reset each call to move() or score()
    //
    //////////////////////////////////////////////////////////

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
        this(counter, options, null);
    }

    /**
     * Create with the given counter and options
     *
     * @param counter eval + counter
     * @param options search options
     */
    public MidgameSearcher(Counter counter, String options) {
        this(counter, new Options(options), null);
    }

    public MidgameSearcher(Counter counter, String options, @NotNull Book book) {
        this(counter, new Options(options), book);
    }

    public MidgameSearcher(@NotNull Counter counter, @NotNull Options options, @Nullable Book book) {
        this.options = options;
        this.counter = counter;
        this.book = book;
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
     * @param board   position to search
     * @param moverMoves legal moves to check. If this is a subset of all legal moves, only the subset will
     *                   be checked.
     * @param depth      search depth
     * @return the best move from this position, and its score in centi-disks
     * @throws IllegalArgumentException if the position has no legal moves
     */
    public MoveScore getMoveScore(Board board, long moverMoves, int depth, int width)  {
        try {
            return getMoveScore(board, moverMoves, depth, width, AbortCheck.NEVER);
        } catch (SearchAbortedException e) {
            // this can never happen because we used AbortCheck.NEVER
            throw new IllegalStateException("Shouldn't be here.");
        }
    }

    /**
     * Select a move based on a midgame search.
     * <p/>
     * Precondition: The mover has at least one legal move.
     *
     * @param board   position to search
     * @param moverMoves legal moves to check. If this is a subset of all legal moves, only the subset will
     *                   be checked.
     * @param depth      search depth
     * @param abortCheck test for whether the search should be abandoned
     * @return the best move from this position, and its score in centi-disks
     * @throws IllegalArgumentException if the position has no legal moves
     * @throws SearchAbortedException   if the search was aborted
     */
    public MoveScore getMoveScore(Board board, long moverMoves, int depth, int width, AbortCheck abortCheck) throws SearchAbortedException {
        if (moverMoves == 0) {
            throw new IllegalArgumentException("must have a legal move");
        }

        MidgameSearch search = createSearch(board.nEmpty(), depth, width, abortCheck);
        final BA ba = search.hashMove(board.mover(), board.enemy(), moverMoves, NovelloUtils.NO_MOVE, -NovelloUtils.NO_MOVE, depth);
        String pv = midgameHashTables.extractPv(board, ba.score);
        return new MoveScore(ba.bestMove, ba.score, pv.isEmpty() ? null : pv);
    }

    MidgameSearch createSearch(int nEmpty, int depth, int width, AbortCheck abortCheck) {
        ForkJoinPool pool = new ForkJoinPool();
        return new MidgameSearch(nEmpty, midgameHashTables, options, counter, pool, depth, width, book, abortCheck);
    }

    /**
     * Select a move based on a midgame search.
     * <p/>
     * Precondition: The mover has at least one legal move.
     *
     * @param board   position to search
     * @param alpha      search alpha, in centi-disks
     * @param beta       search beta, in centi-disks
     * @param depth      search depth
     * @param abortCheck test for whether the search should be abandoned
     * @return the score of the position in centi-disks
     * @throws SearchAbortedException if the search was aborted
     */
    public int calcScore(Board board, int alpha, int beta, int depth, int width, AbortCheck abortCheck) throws SearchAbortedException {
        return calcScore(board.mover(), board.enemy(), alpha, beta, depth, width, abortCheck);
    }

    /**
     * @param mover      mover disks
     * @param enemy      enemy disks
     * @param alpha      search alpha, in centi-disks
     * @param beta       search beta, in centi-disks
     * @param depth      remaining search depth, in ply. If &le; 0, returns the eval.
     * @param abortCheck test for whether the search should be abandoned
     * @return score of the position, in centidisks
     * @throws SearchAbortedException if the search was aborted
     */
    private int calcScore(long mover, long enemy, int alpha, int beta, int depth, int width, AbortCheck abortCheck) throws SearchAbortedException {
        MidgameSearch search = createSearch(BitBoardUtils.nEmpty(mover, enemy), depth, width, abortCheck);

        return search.searchScore(mover, enemy, alpha, beta, depth);
    }

    /**
     * Return a score estimate based on a midgame search.
     * <p/>
     * The mover does not need to have a legal move - if he doesn't this method will pass or return a terminal value as
     * necessary.
     *
     * @param board position to evaluate
     * @param depth    search depth. If &le; 0, returns the eval.
     * @return score of the move.
     */
    public int calcScore(Board board, int depth, int width) {
        return calcScore(board.mover(), board.enemy(), depth, width);
    }

    /**
     * Return a score estimate based on a midgame search.
     * <p/>
     * The mover does not need to have a legal move - if he doesn't this method will pass or return a terminal value as
     * necessary.
     * <p/>
     * This is a full-width search that never aborts.
     *
     * @param mover mover disks
     * @param enemy enemy disks
     * @param depth search depth. If &le; 0, returns the eval.
     * @return score of the move.
     */
    public int calcScore(long mover, long enemy, int depth, int width) {
        try {

            return calcScore(mover, enemy, NovelloUtils.NO_MOVE, -NovelloUtils.NO_MOVE, depth, width, AbortCheck.NEVER);
        } catch (SearchAbortedException e) {
            // this can never happen because we used AbortCheck.NEVER
            throw new IllegalStateException("Shouldn't be here.");
        }
    }

    public void clear() {
        midgameHashTables.clear(63);
    }

    /**
     * calculate a MoveScore for a specific move.
     *
     *
     * @param sq sq of move to check
     * @param pos position before sq has been played
     * @param alpha pos POV
     * @param beta  pos POV
     * @param subDepth depth remaining from subPos
     * @return MoveScore, POV original position (not subPos).
     * @throws SearchAbortedException
     */
    public MoveScore calcSubMoveScore(int sq, Board pos, int alpha, int beta, int subDepth, int width, AbortCheck abortCheck) throws SearchAbortedException {
        final int score;
        final Board subPos = pos.play(sq);
        score = -calcScore(subPos, -beta, -alpha, subDepth, width, abortCheck);
        final String subPv = midgameHashTables.extractPv(subPos, -score);
        String pv = BitBoardUtils.sqToLowerText(sq);
        if (subPv != null) {
            pv += "-" + subPv;
        }
        return new MoveScore(sq, score, pv);
    }
    /**
     * Midgame search options.
     * <p/>
     * Options are characters interpreted as flags.
     * Current flags are:
     * <p/>
     * S = non-strong engine (don't use variable search depths)<br/>
     * w = full-width search (don't use MPC)<br/>
     * x = experimental<br/>
     */
    public static class Options {
        final boolean mpc;
        public final boolean variableEndgame;
        public final boolean variableMidgame;
        final boolean printSearch;
        public final boolean experimental;

        public Options(String options) {
            mpc = !options.contains("w");
            variableEndgame = !options.contains("S");
            variableMidgame = options.contains("v");
            printSearch = options.contains("p");
            experimental = options.contains("x");
        }
    }
}
