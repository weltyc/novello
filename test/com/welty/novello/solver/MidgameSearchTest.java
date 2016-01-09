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
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.DiskEval;
import com.welty.novello.eval.Eval;
import com.welty.novello.external.api.AbortCheck;
import junit.framework.TestCase;

public class MidgameSearchTest extends TestCase {
    public void testTreeMove() throws SearchAbortedException {
        final Eval eval = new DiskEval();
        final MidgameSearcher midgameSearcher = new MidgameSearcher(new Counter(eval));
        final Board board = Board.of("-------- -------- -------- --OO---- --*O*--- ----OO-- -------- -------- *");
        final MidgameSearch search = midgameSearcher.createSearch(board.nEmpty(), 1, 0, AbortCheck.NEVER);

        final long mover = board.mover();
        final long enemy = board.enemy();
        final long moverMoves = board.calcMoves();

        // so the tests are readable
        final int c3 = BitBoardUtils.textToSq("C3");
        final int e3 = BitBoardUtils.textToSq("E3");
        final int g7 = BitBoardUtils.textToSq("G7");

        // true value is within the window
        BA ba = search.hashMove(mover, enemy, moverMoves, -6400, 6400, 1);
        assertEquals(200, ba.score);
        assertEquals(c3, ba.bestMove);

        // true value is above the window
        ba = search.hashMove(mover, enemy, moverMoves, -6400, 80, 1);
        assertTrue(100 <= ba.score);
        assertTrue(ba.score <= 200);
        assertTrue(ba.bestMove == c3 || ba.bestMove == e3 || ba.bestMove == g7);

        // true value is at the bottom of the window
        midgameSearcher.clear();
        ba = search.hashMove(mover, enemy, moverMoves, 200, 6400, 1);
        assertEquals(200, ba.score);
        assertEquals(-1, ba.bestMove);

        // true value is below the window
        ba = search.hashMove(mover, enemy, moverMoves, 300, 6400, 1);
        assertEquals(200, ba.score); // required by fail-soft. Fail-hard would return 300.
        assertEquals(-1, ba.bestMove);
    }


    public void testSolverAlpha() {
        for (int i = -6400; i <= 6400; i++) {
            final int expected = (int) Math.floor(i / (double) CoefficientCalculator.DISK_VALUE);
            assertEquals("" + i, expected, MidgameSearch.solverAlpha(i));
        }
        assertEquals(-64, MidgameSearch.solverAlpha(-100000));
    }

    public void testSolverBeta() {
        for (int i = -6400; i <= 6400; i++) {
            final int expected = (int) Math.ceil(i / (double) CoefficientCalculator.DISK_VALUE);
            assertEquals("" + i, expected, MidgameSearch.solverBeta(i));
        }
        assertEquals(64, MidgameSearch.solverBeta(100000));
    }
}
