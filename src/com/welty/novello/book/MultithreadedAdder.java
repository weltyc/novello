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

package com.welty.novello.book;

import com.welty.novello.core.Board;
import com.welty.novello.core.MoveScore;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SearchDepths;
import com.welty.novello.solver.Counter;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.novello.solver.Solver;
import org.jetbrains.annotations.Nullable;

public class MultithreadedAdder implements Adder {
    @Nullable private final Book book;
    private final int midgameDepth;
    private final int solveDepth;
    private final ThreadLocal<Searcher> searchers = new ThreadLocal<Searcher>() {
        @Override protected Searcher initialValue() {
            return new Searcher(book);
        }
    };

    public MultithreadedAdder(@Nullable Book book, int midgameDepth) {
        this.book = book;
        this.midgameDepth = midgameDepth;
        this.solveDepth = SearchDepths.calcSolveDepth(midgameDepth);
    }

    @Override public MoveScore calcDeviation(Board board, long moves) {
//            System.out.print("d");
        return searchers.get().mid.calcMoveIterative(board, moves, midgameDepth);
    }

    @Override public MoveScore solve(Board board) {
//            System.out.print("s");
        return searchers.get().end.getMoveScore(board.mover(), board.enemy());
    }

    @Override public int solveDepth() {
        return solveDepth;
    }


    private static class Searcher {
        private final MidgameSearcher mid;
        private final Solver end;

        Searcher(Book book) {
            final Eval eval = Players.currentEval();
            final Counter counter = new Counter(eval);
            final MidgameSearcher.Options options = new MidgameSearcher.Options("");
            mid = new MidgameSearcher(counter, options, book);
            end = new Solver(eval, options, book);
        }
    }

}
