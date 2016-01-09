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
import com.welty.novello.book.BookTest;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Board;
import com.welty.novello.core.Counts;
import com.welty.novello.core.MoveScore;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.Players;
import com.welty.novello.external.api.AbortCheck;
import junit.framework.TestCase;

import static com.welty.novello.eval.CoefficientCalculator.DISK_VALUE;

/**
 */
public class MidgameSearcherTest extends TestCase {
    public void test1PlySearch() throws Exception {
        final Eval eval = Players.currentEval();
        final Counter counter = new Counter(eval);
        final MidgameSearcher midgameSearcher = new MidgameSearcher(counter, "w");

        final Board prev = Board.of("--------\n" +
                "--------\n" +
                "-----*--\n" +
                "---***--\n" +
                "---***--\n" +
                "---OO*--\n" +
                "------*-\n" +
                "--------\n" +
                "O");


        final long moves = prev.calcMoves();
        final int width = 0;
        final MoveScore moveScore = midgameSearcher.getMoveScore(prev, moves, 1, width, AbortCheck.NEVER);
        assertTrue("must be a legal move", BitBoardUtils.isBitSet(moves, moveScore.sq));
        assertEquals(Long.bitCount(moves), counter.nFlips());

        final Board terminal = prev.play(moveScore.sq);
        assertEquals(-eval.eval(terminal), moveScore.centidisks);

        int score = simpleSearch(eval, prev, moves);
        assertEquals(score, moveScore.centidisks);
    }

    // do a simple 1-ply search without sorting.
    private static int simpleSearch(Eval eval, Board prev, long moves) {
        int score = Integer.MIN_VALUE;
        for (long m = moves; m != 0; ) {
            final int sq = Long.numberOfTrailingZeros(m);
            m &= ~(1L << sq);
            final Board sub = prev.play(sq);
            final int subScore = -eval.eval(sub);
            if (subScore > score) {
                score = subScore;
            }
        }
        return score;
    }

    public void testSearchScoreWithPass() {
        final Eval eval = Players.currentEval();
        final MidgameSearcher midgameSearcher = new MidgameSearcher(new Counter(eval), "w");

        final Board root = Board.of("--OO-O-O\n" +
                "--****OO\n" +
                "*--*OOOO\n" +
                "-***OOOO\n" +
                "-***O*OO\n" +
                "-*OO**-O\n" +
                "*****-*-\n" +
                "OOOOOO-*\n" +
                "O");
        // g1  pass e1 is the best line

//        player.calcMove(root, root.calcMoves(), -1);
        final Board g1 = root.play("G1");
        final int subScore = -midgameSearcher.calcScore(g1, 1, 0);
        // had a bug where it was returning the terminal value (+6) if the opponent passes. This position is way
        // better than that!
        assertTrue(subScore > 20 * DISK_VALUE);
    }

    public void testMpc() {
        for (int depth = 4; depth <= 6; depth += 2) {
            // simple test: 4 ply search should give the same result with fewer nodes
            final Eval eval = Players.currentEval();
            final MidgameSearcher mpcSearcher = new MidgameSearcher(new Counter(eval));
            final MidgameSearcher fwSearcher = new MidgameSearcher(new Counter(eval), "w");
            final Board board = Board.of("-------- -------- -------- --OOO--- --*O*--- ----OO-- -------- -------- *");
            final int fwScore = fwSearcher.calcScore(board, depth, 0);
            final long n = fwSearcher.getCounts().nFlips;
            final int mpcScore = mpcSearcher.calcScore(board, depth, 0);
            final long nMpc = mpcSearcher.getCounts().nFlips;
            assertTrue("scores should be similar", Math.abs(fwScore - mpcScore) < 100);
            System.out.println("MPC used " + nMpc + ", full-width used " + n);
            assertTrue("MPC should give the same result with fewer nodes", nMpc < n);
        }
    }

    public void testFlipToEvalRatio() {
        final Eval eval = Players.currentEval();
        final MidgameSearcher midgameSearcher = new MidgameSearcher(new Counter(eval), "w");
        final int depth = 4;

        Board board = Board.START_BOARD;
        midgameSearcher.getMoveScore(board, board.calcMoves(), depth, 0);
        final Counts counts = midgameSearcher.getCounts();
        final long nFlips = counts.nFlips;
        final long nEvals = counts.nEvals;
        System.out.format("%,d flips and %,d evals", nFlips, nEvals);
        assertTrue(nFlips < nEvals * 2);
    }

    public void testLoadFromBook() {
        // midgame searcher should use book values for the positions
        final Eval eval = Players.currentEval();
        final Book book = BookTest.sampleBook();
        final MidgameSearcher midgameSearcher = new MidgameSearcher(new Counter(eval), "w", book);


        final Board board = Board.START_BOARD.play("F5");
        final MoveScore moveScore = midgameSearcher.getMoveScore(board, board.calcMoves(), 1, 1);
        assertEquals(200, moveScore.centidisks);
    }
}
