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

package com.welty.novello.eval;

import com.welty.novello.core.Board;
import com.welty.novello.core.Me;
import junit.framework.TestCase;

import java.util.Arrays;

import static com.welty.novello.core.BitBoardUtils.reflectHorizontally;
import static com.welty.novello.core.BitBoardUtils.reflectVertically;
import static org.junit.Assert.assertArrayEquals;

/**
 */
public class CornerBlockTermTest extends TestCase {
    public void testInstance() {
        testInstance(0, 0, 0);
        testInstance(1, 1, 0);
        testInstance(2, 0, 1);
        testInstance(6, 0, 2);
        testInstance(6, 0, 0xFFFFFFFFFFF8F8FAL);
        testInstance(27, 0x0100, 0);
        testInstance(81, 0x0200, 0);
        testInstance(243, 0x0400, 0);
        testInstance(729, 0x010000, 0);
        testInstance(2187, 0x020000, 0);
        testInstance(6561, 0x040000, 0);
    }

    private void testInstance(int instance, long mover, long enemy) {
        testInstance(false, false, instance, mover, enemy);
        testInstance(false, true, instance, reflectHorizontally(mover), reflectHorizontally(enemy));
        testInstance(true, false, instance, reflectVertically(mover), reflectVertically(enemy));
        testInstance(true, true, instance, Long.reverse(mover), Long.reverse(enemy));
    }

    private void testInstance(boolean top, boolean left, int instance, long mover, long enemy) {
        final CornerBlockTerm term = new CornerBlockTerm(top, left);
        assertEquals(top + "-" + left, instance, term.instance(mover, enemy, 0, 0));
    }

    public void testReflectionOrids() {
        testReflectionOrids(Me.early);
        testReflectionOrids(Me.late);
    }

    private static void testReflectionOrids(Me tp) {
        final Board board = tp.toPosition();
        int[] expected = calcOrids(board, 0);
        for (int r=1; r<8; r++) {
            assertArrayEquals(""+r, expected, calcOrids(board, r));
        }
    }

    private static int[] calcOrids(Board board, int r) {
        int[] expected = new int[4];
        Term[] terms = {
                new CornerBlockTerm(false, false),
                new CornerBlockTerm(false, true),
                new CornerBlockTerm(true, false),
                new CornerBlockTerm(true, true)
        };
        final Board reflection = board.reflection(r);
        for (int i=0; i<4; i++) {
            expected[i]=terms[i].orid(reflection.mover(), reflection.enemy(), 0, 0);
        }
        Arrays.sort(expected);
        return expected;
    }
}
