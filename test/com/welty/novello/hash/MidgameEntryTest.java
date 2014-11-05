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

package com.welty.novello.hash;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Board;
import junit.framework.TestCase;

import static com.welty.novello.core.NovelloUtils.NO_MOVE;

/**
 */
public class MidgameEntryTest extends TestCase {
    private static final Board pos = Board.START_BOARD;
    private static final long mover = pos.mover();
    private static final long enemy = pos.enemy();
    private static final int f5 = BitBoardUtils.textToSq("F5");
    private static final int c3 = BitBoardUtils.textToSq("C3");
    private static final int score = 13;

    final MidgameEntry entry = new MidgameEntry();

    @Override protected void setUp() throws Exception {
        clear();
    }

    public void testIsExact() {
        final MidgameHashTables ht = new MidgameHashTables();
        MidgameEntry entry = ht.getEntry(mover, enemy);
        assertFalse("nothing in there yet", entry.matches(mover, enemy));

        // Store an exact value. We know it's exact because the alpha < score < beta.
        ht.store(mover, enemy, NO_MOVE, -NO_MOVE, 1, 0, f5, score);

        assertTrue(entry.matches(mover, enemy));
        assertEquals(score, entry.getMin());
        assertEquals(score, entry.getMax());
        assertEquals(1, entry.getDepth());
        assertEquals(f5, entry.getBestMove());
    }

    public void testClear() {
        update(NO_MOVE, -NO_MOVE, 12, f5, score);
        clear();
        assertFalse(entry.matches(mover, enemy));
        assertEquals("Need to be able to use this entry with depth-first", -1, entry.getDepth());
    }

    public void testUpdatesFromEmpty() {
        final int depth = 1;

        // pv
        update(NO_MOVE, -NO_MOVE, depth, f5, score);
        check(score, score, f5, depth);

        // cut 1
        clear();
        update(-1, 0, depth, f5, score);
        check(score, -NO_MOVE, f5, depth);

        // cut 2
        clear();
        update(score-1, score, depth, f5, score);
        check(score, -NO_MOVE, f5, depth);

        // all 1
        clear();
        update(score, score+1, depth, -1, score);
        check(NO_MOVE, score, -1, depth);

        // all 2
        clear();
        update(score+10, score+20, depth, -1, score);
        check(NO_MOVE, score, -1, depth);
    }

    public void testUpdatesFromLowerDepth() {
        final int depth = 2;

        // pv
        setDepth1();
        update(NO_MOVE, -NO_MOVE, depth, f5, score);
        check(score, score, f5, depth);

        // cut 1
        setDepth1();
        update(-1, 0, depth, f5, score);
        check(score, -NO_MOVE, f5, depth);

        // cut 2
        setDepth1();
        update(score-1, score, depth, f5, score);
        check(score, -NO_MOVE, f5, depth);

        // all 1.
        // since we don't provide a best move, it keeps the old one.
        setDepth1();
        update(score, score+1, depth, -1, score);
        check(NO_MOVE, score, f5, depth);

        // all 2
        setDepth1();
        update(score+10, score+20, depth, -1, score);
        check(NO_MOVE, score, f5, depth);

        // overwrite best move
        setDepth1();
        update(NO_MOVE, -NO_MOVE, depth, c3, score);
        check(score, score, c3, depth);
    }

    public void testUpdatesFromSameDepthExact() {
        final int depth = 1;
        final int oldScore = MidgameEntryTest.score; // 13
        final int score = 16; // higher than the previously stored score of 13

        // pv
        setDepth1();
        update(NO_MOVE, -NO_MOVE, depth, f5, score);
        check(score, score, f5, depth);

        // cut 1
        setDepth1();
        update(score-20, score-10, depth, f5, score);
        check(score, score, f5, depth);  // Happens due to MPC or TT. Since we had an exact score before, let's have an exact score now.

        // cut 2
        setDepth1();
        update(score-1, score, depth, f5, score);
        check(score, score, f5, depth);  // Happens due to MPC or TT. Since we had an exact score before, let's have an exact score now.

        // all 1.
        // since we don't provide a best move, it keeps the old one.
        setDepth1();
        update(score, score+1, depth, -1, score);
        check(oldScore, oldScore, f5, depth); // Happens normally. Old value = 13, new value <= 16, so keep the 13.

        // all 2
        setDepth1();
        update(score+10, score+20, depth, -1, score); // Happens normally. Old value = 13, new value <= 16, so keep the 13.
        check(oldScore, oldScore, f5, depth);

        // overwrite best move
        setDepth1();
        update(NO_MOVE, -NO_MOVE, depth, c3, score);
        check(score, score, c3, depth);
    }

    private void setDepth1() {
        entry.clear();
        entry.update(mover, enemy, NO_MOVE, -NO_MOVE, 1, 0, f5, score);
    }

    public void testUpdatesFromSameDepthAll() {
        final int depth = 1;
        final int oldScore = MidgameEntryTest.score; // 13
        final int score = 16; // higher than the previously stored score of 13

        // pv
        setDepth1All();
        update(NO_MOVE, -NO_MOVE, depth, f5, score);
        check(score, score, f5, depth);

        // cut 1
        setDepth1All();
        update(score-20, score-10, depth, f5, score);
        check(score, score, f5, depth);  // Happens due to MPC or TT. Since score <= 13 before, let's have an exact score now.

        // cut 2
        setDepth1All();
        update(score-1, score, depth, f5, score);
        check(score, score, f5, depth);  // Happens due to MPC or TT. Since score <= 13 before, let's have an exact score now.

        // all 1.
        // since we don't provide a best move, it keeps the old one.
        setDepth1All();
        update(score, score+1, depth, -1, score);
        check(NO_MOVE, oldScore, -1, depth); // Happens normally. Old value <= 13, new value <= 16, so keep the 13.

        // all 2
        setDepth1All();
        update(score+10, score+20, depth, -1, score); // Happens normally. Old value <= 13, new value <= 16, so keep the 13.
        check(NO_MOVE, oldScore, -1, depth);

        // all 3
        setDepth1All();
        update(5, -NO_MOVE, depth, -1, 5); // Happens normally. Was <=13, now <=5.
        check(NO_MOVE, 5, -1, depth);

        // overwrite best move
        setDepth1All();
        update(NO_MOVE, -NO_MOVE, depth, c3, score);
        check(score, score, c3, depth);
    }

    private void setDepth1All() {
        entry.clear();
        entry.update(mover, enemy, score, -NO_MOVE, 1, 0, -1, score);
    }

    public void testUpdatesFromSameDepthCut() {
        final int depth = 1;
        final int oldScore = MidgameEntryTest.score; // 13
        final int score = 16; // higher than the previously stored score of 13

        // pv
        setDepth1Cut();
        update(NO_MOVE, -NO_MOVE, depth, f5, score);
        check(score, score, f5, depth);

        // cut 1
        setDepth1Cut();
        update(score-20, score-10, depth, f5, score);
        check(score, -NO_MOVE, f5, depth);  // score was >=13, now >=16.

        // cut 2
        setDepth1Cut();
        update(score-1, score, depth, f5, score);
        check(score, -NO_MOVE, f5, depth);  // score was >=13, now >=16.

        // all 1.
        // since we don't provide a best move, it keeps the old one.
        setDepth1Cut();
        update(score, score+1, depth, -1, score);
        check(oldScore, score, f5, depth); // Happens normally. Old value >= 13, new value <= 16, so range is 13-16

        // all 2
        setDepth1Cut();
        update(score+10, score+20, depth, -1, score); // Happens normally. Old value >= 13, new value <= 16, so keep the 13.
        check(oldScore, score, f5, depth); // Happens normally. Old value >= 13, new value <= 16, so range is 13-16

        // all 3
        setDepth1Cut();
        update(5, -NO_MOVE, depth, -1, 5); // Happens from MPC or TT. Was >=13, now <=5.
        check(5, 5, f5, depth);

        // overwrite best move
        setDepth1Cut();
        update(NO_MOVE, -NO_MOVE, depth, c3, score);
        check(score, score, c3, depth);
    }

    private void setDepth1Cut() {
        entry.clear();
        entry.update(mover, enemy, NO_MOVE, score, 1, 0, f5, score);
    }

    public void testUpdatesFromHigherDepth() {
        final int depth = 0;
        final int oldScore = MidgameEntryTest.score; // 13
        final int score = 16; // higher than the previously stored score of 13

        // lower depth should get totally ignored.
        setDepth1();
        update(NO_MOVE, -NO_MOVE, depth, c3, score);
        check(oldScore, oldScore, f5, 1);
    }

    private void update(int alpha, int beta, int depth, int bestMove, int score) {
        entry.update(mover, enemy, alpha, beta, depth, 0, bestMove, score);
    }

    private void clear() {
        entry.clear();
    }

    private void check(int min, int max, int bestMove, int depth) {
        checkEntry(mover, enemy, min, max, bestMove, depth);
    }

    private void checkEntry(long mover, long enemy, int min, int max, int bestMove, int depth) {
        assertTrue(entry.matches(mover, enemy));
        assertEquals("min", min, entry.getMin());
        assertEquals("max", max, entry.getMax());
        assertEquals("best move", bestMove, entry.getBestMove());
        assertEquals("depth", depth, entry.getDepth());
    }

    public void testIsDeepEnough() {
        entry.clear();
        entry.update(mover, enemy, NO_MOVE, -NO_MOVE, 1, 1, f5, score);
        assertTrue(entry.deepEnoughToSearch(1, 1));
        assertTrue(entry.deepEnoughToSearch(0, 1));
        assertTrue(entry.deepEnoughToSearch(1, 0));
        assertFalse(entry.deepEnoughToSearch(1, 2));
        assertFalse(entry.deepEnoughToSearch(2, 1));
    }

    public void testWidthUpdating() {
        entry.clear();
        entry.update(mover, enemy, NO_MOVE, -NO_MOVE, 1, 1, f5, score);
        // this update should fail because it's a narrower width
        entry.update(mover, enemy, NO_MOVE, -NO_MOVE, 1, 0, c3, score-1);
        assertEquals(f5, entry.getBestMove());
        assertEquals(score, entry.getMin());
        assertEquals(score, entry.getMax());
    }
}
