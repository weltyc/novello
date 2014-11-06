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
import com.welty.othello.c.CReader;
import com.welty.othello.gdk.COsBoard;

/**
 */
public class BoardTest extends BitBoardTestCase {
    public void testConstructor() {
        final Board bb = new Board(0x01020304050607L, 0x10203040506070L, true);
        final String positionString = bb.positionString();
        assertEquals(bb, Board.of(positionString));
    }

    public void testOfUsingFunnyChars() {
        final Board bb = Board.of("-------- -------- -------- -------- -------- -------- -------- -------- X");
        assertTrue(bb.blackToMove);
    }

    public void testMinimalReflection() {
        final long black = 0x3141592653589793L;
        final long white = 0x2718281828459045L & ~black;

        final Board bb = new Board(black, white, true);
        final MinimalReflection minimal = bb.minimalReflection();
        for (int r = 0; r < 8; r++) {
            assertEquals(minimal, bb.reflection(r).minimalReflection());
        }
    }

    public void testOfOsPosition() {
        final COsBoard board = new COsBoard(new CReader("8 " + Board.START_BOARD.positionString("")));
        assertEquals(Board.START_BOARD, Board.of(board));
    }
}
