package com.welty.novello.core;

import junit.framework.TestCase;

/**
 */
public class Move8x8Test extends TestCase {
    public void testSquare() {
        testSquare(63, "A1//0.02");
        testSquare(0, "h8");
        testSquare(7, "A8   ");
        testSquare(-1, "pass");
        testSquare(-1, "pa");
        testSquare(-1, "PA");
        testSquare(-1, "PASS");
    }

    private void testSquare(int sq, String moveText) {
        assertEquals(sq, new Move8x8(moveText).getSq());
    }

    public void testConstructFromMoveScore() {
        testConstructFromMoveScore("H8", 0);
        testConstructFromMoveScore("PA", -1);
    }

    private static void testConstructFromMoveScore(String square, int sq) {
        final Move8x8 move8x8 = new Move8x8(new MoveScore(sq, 1), 2);
        assertEquals(sq, move8x8.getSq());
        assertEquals(square, move8x8.move.toString());
    }
}
