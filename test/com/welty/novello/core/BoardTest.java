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
        final Board minimal = bb.minimalReflection();
        for (int r = 0; r < 8; r++) {
            final Board reflection = bb.reflection(r);
            assertTrue(minimal.compareTo(reflection) < 0 || minimal.equals(reflection));
        }
    }

    public void testOfOsPosition() {
        final COsBoard board = new COsBoard(new CReader("8 " + Board.START_BOARD.positionString("")));
        assertEquals(Board.START_BOARD, Board.of(board));
    }
}
