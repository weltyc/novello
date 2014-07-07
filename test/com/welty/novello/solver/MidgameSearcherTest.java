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

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Board;
import com.welty.novello.core.Counts;
import com.welty.novello.core.MoveScore;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.DiskEval;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.Players;
import com.welty.othello.api.AbortCheck;
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
        final MoveScore moveScore = midgameSearcher.getMoveScore(prev, moves, 1, AbortCheck.NEVER);
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
        final int subScore = -midgameSearcher.calcScore(g1, 1);
        // had a bug where it was returning the terminal value (+6) if the opponent passes. This position is way
        // better than that!
        assertTrue(subScore > 20 * DISK_VALUE);
    }

    public void testTreeMove() throws SearchAbortedException {
        final Eval eval = new DiskEval();
        final MidgameSearcher midgameSearcher = new MidgameSearcher(new Counter(eval));
        final Board board = Board.of("-------- -------- -------- --OO---- --*O*--- ----OO-- -------- -------- *");
        final long mover = board.mover();
        final long enemy = board.enemy();
        final long moverMoves = board.calcMoves();

        // so the tests are readable
        final int c3 = BitBoardUtils.textToSq("C3");
        final int e3 = BitBoardUtils.textToSq("E3");
        final int g7 = BitBoardUtils.textToSq("G7");

        // true value is within the window
        MidgameSearcher.BA ba = midgameSearcher.hashMove(mover, enemy, moverMoves, -6400, 6400, 1);
        assertEquals(200, ba.score);
        assertEquals(c3, ba.bestMove);

        // true value is above the window
        ba = midgameSearcher.hashMove(mover, enemy, moverMoves, -6400, 80, 1);
        assertTrue(100 <= ba.score);
        assertTrue(ba.score <= 200);
        assertTrue(ba.bestMove == c3 || ba.bestMove == e3 || ba.bestMove == g7);

        // true value is at the bottom of the window
        midgameSearcher.clear();
        ba = midgameSearcher.hashMove(mover, enemy, moverMoves, 200, 6400, 1);
        assertEquals(200, ba.score);
        assertEquals(-1, ba.bestMove);

        // true value is below the window
        ba = midgameSearcher.hashMove(mover, enemy, moverMoves, 300, 6400, 1);
        assertEquals(200, ba.score); // required by fail-soft. Fail-hard would return 300.
        assertEquals(-1, ba.bestMove);
    }

    public void testMpc() {
        for (int depth = 4; depth <= 6; depth += 2) {
            // simple test: 4 ply search should give the same result with fewer nodes
            final Eval eval = Players.currentEval();
            final MidgameSearcher mpcSearcher = new MidgameSearcher(new Counter(eval));
            final MidgameSearcher fwSearcher = new MidgameSearcher(new Counter(eval), "w");
            final Board board = Board.of("-------- -------- -------- --OOO--- --*O*--- ----OO-- -------- -------- *");
            final int fwScore = fwSearcher.calcScore(board, depth);
            final long n = fwSearcher.getCounts().nFlips;
            final int mpcScore = mpcSearcher.calcScore(board, depth);
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
        midgameSearcher.getMoveScore(board, board.calcMoves(), depth);
        final Counts counts = midgameSearcher.getCounts();
        final long nFlips = counts.nFlips;
        final long nEvals = counts.nEvals;
        System.out.format("%,d flips and %,d evals", nFlips, nEvals);
        assertTrue(nFlips < nEvals * 2);
    }

    public void testSolverAlpha() {
        for (int i = -6400; i <= 6400; i++) {
            final int expected = (int) Math.floor(i / (double) CoefficientCalculator.DISK_VALUE);
            assertEquals("" + i, expected, MidgameSearcher.solverAlpha(i));
        }
        assertEquals(-64, MidgameSearcher.solverAlpha(-100000));
    }

    public void testSolverBeta() {
        for (int i = -6400; i <= 6400; i++) {
            final int expected = (int) Math.ceil(i / (double) CoefficientCalculator.DISK_VALUE);
            assertEquals("" + i, expected, MidgameSearcher.solverBeta(i));
        }
        assertEquals(64, MidgameSearcher.solverBeta(100000));
    }
}
