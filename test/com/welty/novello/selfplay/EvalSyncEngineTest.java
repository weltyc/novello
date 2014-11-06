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

package com.welty.novello.selfplay;

import com.welty.novello.book.Book;
import com.welty.novello.book.BookTest;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Board;
import com.welty.novello.core.MoveScore;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.SearchAbortedException;
import com.welty.othello.api.AbortCheck;
import com.welty.othello.gdk.OsClock;
import com.welty.othello.protocol.Depth;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

public class EvalSyncEngineTest extends TestCase {
    public void test1PlySearch() throws Exception {
        final Eval eval = Players.currentEval();
        final EvalSyncEngine engine = new EvalSyncEngine(eval, "", eval.toString());

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
        final MoveScore moveScore = engine.calcMove(prev, null, 1);
        assertTrue("must be a legal move", BitBoardUtils.isBitSet(moves, moveScore.sq));

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

    /**
     * Solve value was 100x too high in Viewer, printing out "+400 disks" instead of "+4 disks".
     */
    public void testSolveValue() {
        final Eval eval = Players.currentEval();
        final EvalSyncEngine player = new EvalSyncEngine(eval, "", eval.toString());

        final Board prev = Board.of("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*. O");

        assertEquals(6400, player.calcMove(prev, null, 1).centidisks);
    }

    public void testInsertSorted() {
        ArrayList<MoveScore> moveScores = new ArrayList<>();
        final MoveScore m30 = new MoveScore(30, 30);
        final MoveScore m20 = new MoveScore(20, 20);
        final MoveScore m10 = new MoveScore(10, 10);
        moveScores.add(m30);
        moveScores.add(m20);
        moveScores.add(m10);
        final MoveScore m15 = new MoveScore(20, 15);
        EvalSyncEngine.insertSorted(moveScores, 1, m15);
        assertEquals(Arrays.asList(m30, m15, m10), moveScores);

        final MoveScore m40 = new MoveScore(10, 40);
        EvalSyncEngine.insertSorted(moveScores, 2, m40);
        assertEquals(Arrays.asList(m40, m30, m15), moveScores);
    }

    public void testCalcTargetMillis60() {
        final double seconds = EvalSyncEngine.calcTargetTime(new OsClock(60), 60);
        assertTrue(seconds > 1);
        assertTrue(seconds < 5);
    }

    public void testCalcTargetMillis34() {
        final OsClock clock = new OsClock(27.407);
        final double seconds = EvalSyncEngine.calcTargetTime(clock, 34);
        assertTrue(seconds > 1);
        assertTrue(seconds < 10);
    }

    public void testCalcTargetMillis26() {
        final OsClock clock = new OsClock(3.751);
        final double seconds = EvalSyncEngine.calcTargetTime(clock, 26);
        assertTrue(seconds > 0.1);
        assertTrue(seconds < 2);
    }

    public void testCalcTargetMillis18() {
        final OsClock clock = new OsClock(1.071);
        final double seconds = EvalSyncEngine.calcTargetTime(clock, 18);
        assertTrue(seconds > 0.1);
        assertTrue(seconds < 2);
    }

    public void testCalcTargetMillis2() {
        final OsClock clock = new OsClock(1.62);
        final double seconds = EvalSyncEngine.calcTargetTime(clock, 2);
        assertTrue(seconds > 0.25);
        assertTrue(seconds < 1.62);
    }

    public void testBookSearch() throws SearchAbortedException {
        final EvalSyncEngine engine = createBookEngine();
        final Board f5 = Board.START_BOARD.play("F5");
        final EvalSyncEngine.Listener listener = Mockito.mock(EvalSyncEngine.Listener.class);
        engine.calcHints(f5, 6, 2, AbortCheck.NEVER, listener);
        Mockito.verify(listener).hint(new MoveScore("d6", +200), new Depth(1), true);
        Mockito.verify(listener).hint(new MoveScore("f4", -400), new Depth(0), true);
        Mockito.verifyNoMoreInteractions(listener);
    }

    private static EvalSyncEngine createBookEngine() {
        Book book = BookTest.sampleBook();

        final Eval eval = Players.currentEval();
        return new EvalSyncEngine(eval, "", eval.toString(), book);
    }

    public void testMoveFromBook() {
        final EvalSyncEngine engine = createBookEngine();

        // a move from book should be instant.
        final EvalSyncEngine.Listener listener = Mockito.mock(EvalSyncEngine.Listener.class);
        final MoveScore moveScore = engine.calcMove(Board.START_BOARD.play("F5"), new OsClock(10), 2, AbortCheck.NEVER, listener);
        assertEquals(new MoveScore("d6", 200), moveScore);
        Mockito.verifyNoMoreInteractions(listener);
    }
}
