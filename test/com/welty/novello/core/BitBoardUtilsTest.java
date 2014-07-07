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

package com.welty.novello.core;

import com.welty.novello.solver.BitBoardTestCase;

import java.util.Random;

import static com.welty.novello.core.BitBoardUtils.*;

/**
 */
public class BitBoardUtilsTest extends BitBoardTestCase {
    @SuppressWarnings("OctalInteger")
    public void testReflectDiagonally() throws Exception {
        // note, given an index in octal this just switches row and col.
        testReflectDiagonally(0x01, 0x01);
        testReflectDiagonally(1L<<010, 1L<<001);
        testReflectDiagonally(1L<<007, 1L<<070);
        testReflectDiagonally(1L<<024, 1L<<042);
        testReflectDiagonally(1L<<053, 1L<<035);

    }

    private void testReflectDiagonally(long a, long b) {
        assertBitBoardEquals(a, reflectDiagonally(b));
        assertBitBoardEquals(b, reflectDiagonally(a));
    }

    /**
     * Test BitBoardUtils.calcMoves
     */
    public void testCalcMoves() {
        Board board = new Board("---------------------------O*------*O---------------------------", true);
        testCalcMoves(0x0000102004080000L, board.mover(), board.enemy());
        testCalcMoves(0x0000080420100000L, board.enemy(), board.mover());

        // test to ensure we don't wrap around
        testCalcMoves(0, "-------- O*------ ------*O -------O *------- -------* O------- --------");

        // test diagonals
        testCalcMoves(0x0000240000240000L, "*------* -O----O- -------- -------- -------- -------- -O----O- *------*");

        // Test the maximum number of enemy discs (6)
        testCalcMoves(1L<<56, "*OOOOOO- -------- -------- -------- -------- -------- -------- --------");

        // randomly generated test cases
        testCalcMoves(0xA050200A0C000008L, ".*.OOO*OO...***OOO.O*OOOO*O*.O.*****..**.***OO***O*O**O*OOO*.OO*");
        testCalcMoves(0x04009450001F0830L, "**.*O.O*OO*OO*OO.OO.O..**.O.*..*.**O*.****O.....**OO.OO.O*..***.");

        // generate random test cases
//        Random random = new Random(1337);
//        generateRandomCalcMovesTest(random);
//        generateRandomCalcMovesTest(random);
    }

    private static void generateRandomCalcMovesTest(Random random) {
        final Me tp = Me.early(random);

        final Board board = new Board(tp.mover, tp.enemy, true);
        System.out.format("testCalcMoves(0x%016XL, \"%s\");%n", calcMoves(board.mover(), board.enemy()), board.boardString());
    }

    /**
     * Test all 8 reflections of the mobility calc.
     *
     * @param expected Expected black mobilities.
     * @param boardString Board characters. Black to move.
     */
    private static void testCalcMoves(long expected, String boardString) {
        Board board = new Board(boardString, true);
        testCalcMoves(expected, board.mover(), board.enemy());
    }

    private static void testCalcMoves(long expected, long mover, long enemy) {
        for (int r=0; r<8; r++) {
            final long ex = reflection(expected, r);
            final long mv = reflection(mover, r);
            final long en = reflection(enemy, r);

            assertBitBoardEquals("reflection " + r, ex, calcMoves(mv, en));
        }
    }

    public void testPotMobs() {
        assertBitBoardEquals("no empties", 0, potMobs(0, 0));

        assertBitBoardEquals("empty above", 0x100, potMobs(0x100, 0x10000));
        assertBitBoardEquals("no empty above", 0, potMobs(0x100, 0));
        assertBitBoardEquals("empty above but disk on bottom edge", 0, potMobs(0x1, 0x100));

        assertBitBoardEquals("empty below", 0x100, potMobs(0x100, 0x1));
        assertBitBoardEquals("no empty below", 0, potMobs(0x100, 0));
        assertBitBoardEquals("empty below but disk on top edge", 0, potMobs(1L << 56, 1L << 48));

        assertBitBoardEquals("empty left", 0x40, potMobs(0x40, 0x80));
        assertBitBoardEquals("empty left but disk on right edge", 0, potMobs(0x01, 0x02));

        assertBitBoardEquals("empty right", 0x40, potMobs(0x40, 0x20));
        assertBitBoardEquals("empty right but disk on left edge", 0, potMobs(0x80, 0x40));

        assertBitBoardEquals("empty up left", 0x4000, potMobs(0x4000, 0x800000));
        assertBitBoardEquals("empty up left but disk on right edge", 0, potMobs(0x0100, 0x020000));
        assertBitBoardEquals("empty up left but disk on bottom edge", 0, potMobs(0x02, 0x0400));

        assertBitBoardEquals("empty up right", 0x4000, potMobs(0x4000, 0x200000));
        assertBitBoardEquals("empty up right but disk on left edge", 0, potMobs(0x8000, 0x400000));
        assertBitBoardEquals("empty up right but disk on bottom edge", 0, potMobs(0x40, 0x2000));

        assertBitBoardEquals("empty down left", 0x4000, potMobs(0x4000, 0x80));
        assertBitBoardEquals("empty down left but disk on right edge", 0, potMobs(0x0100, 0x02));
        assertBitBoardEquals("empty down left but disk on top edge", 0, potMobs(0x02L<<56, 0x04L<<48));

        assertBitBoardEquals("empty down right", 0x4000, potMobs(0x4000, 0x20));
        assertBitBoardEquals("empty down right but disk on left edge", 0, potMobs(0x8000, 0x40));
        assertBitBoardEquals("empty down right but disk on top edge", 0, potMobs(0x40L<<56, 0x20L<<48));

    }

    public void testPotMobs2() {
        assertBitBoardEquals("no empties", 0, potMobs2(0, 0));

        assertBitBoardEquals("empty above", 0x10000, potMobs2(0x100, 0x10000));
        assertBitBoardEquals("no empty above", 0, potMobs2(0x100, 0));
        assertBitBoardEquals("empty above but disk on bottom edge", 0, potMobs2(0x1, 0x100));
    }

    public void testLinearPotMob() {
        System.out.println(Me.early);
        assertEquals(8, linearStraightPotMob(Me.early.mover, Me.early.enemy));
        assertEquals(26, linearStraightPotMob(Me.early.enemy, Me.early.mover));
        assertEquals(9, linearPotMob(Me.early.mover, Me.early.enemy));
        assertEquals(50, linearPotMob(Me.early.enemy, Me.early.mover));
    }
}
