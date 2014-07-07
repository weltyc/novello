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
import com.welty.novello.core.Me;
import com.welty.novello.core.Square;

import java.util.Random;

public class StableTest extends BitBoardTestCase {
    public void testEdgeStable() {
        for (int reflection = 0; reflection < 8; reflection++) {
            testEdgeStable(0, 0, 0, reflection);
            testEdgeStable(1, 1, 0, reflection);
            testEdgeStable(1, 0, 1, reflection);
            testEdgeStable(0, 0x70, 0x0E, reflection);
            testEdgeStable(7, 0x35, 0xA, reflection);
        }
    }


    private static void testEdgeStable(int expected, int black, int white, int reflection) {
        final long rBlack = BitBoardUtils.reflection(black, reflection);
        final long rWhite = BitBoardUtils.reflection(white, reflection);
        final long rExpected = BitBoardUtils.reflection(expected, reflection);
        final String msg = String.format("reflection %d [%2x,%2x]", reflection, black, white);
        assertBitBoardEquals(msg, rExpected, Stable.edgeStable(rBlack, rWhite));
    }

    public void testStable() {
        for (int reflection = 0; reflection < 8; reflection++) {
            testStable(0, 0, 0, reflection);
            testStable(1, 1, 0, reflection);
            testStable(1, 0, 1, reflection);
            testStable(0, 0x70, 0x0E, reflection);
            testStable(7, 0x35, 0xA, reflection);
            // stable disk to due everything filled
            testStable(0x0400, 0x0400, 0x04844424150EFF0EL, reflection);
            // stable disk due to next to stable disks
            testStable(0x0F1F, 0x0F1F, 0, reflection);
        }
    }

    public void testRandomPosition() {
        final Random random = new Random(1337);
        for (int i=0; i<100; i++) {
            testRandomPosition(random, i);
        }
    }

    private void testRandomPosition(Random random, int i) {
        final Me pos = Me.late(random);

        final long mover = pos.mover;
        final long enemy = pos.enemy;
        final long stable = Stable.stable(mover, enemy);
        long moves = BitBoardUtils.calcMoves(mover, enemy);
        long moveStable = -1;
        while (moves != 0) {
            final int sq= Long.numberOfTrailingZeros(moves);
            final long loc = 1L<<sq;
            moves ^=loc;

            final long flips = Square.of(sq).calcFlips(mover, enemy);
            moveStable &=~flips;
            moveStable &= Stable.stable(mover | flips | loc, enemy &~flips);
        }
//        System.out.println("=========== " + i + " ============");
//        System.out.println(new Position(mover, enemy, true));
//        System.out.println(new Position(mover&stable, enemy&stable, true));
        assertBitBoardEquals("" + i, moveStable, stable);
    }

    private static void testStable(long expected, long black, long white, int reflection) {
        final long rBlack = BitBoardUtils.reflection(black, reflection);
        final long rWhite = BitBoardUtils.reflection(white, reflection);
        final long rExpected = BitBoardUtils.reflection(expected, reflection);
        final String msg = String.format("reflection %d [%2x,%2x]", reflection, black, white);
        assertBitBoardEquals(msg, rExpected, Stable.stable(rBlack, rWhite));
        assertBitBoardEquals(msg, rExpected, Stable.stable(rWhite, rBlack));
    }

}
