package com.welty.ggf;

import com.welty.novello.core.MoveScore;
import com.welty.novello.eval.CoefficientCalculator;
import junit.framework.TestCase;

/**
 */
public class MoveTest extends TestCase {
    public void testToString() {
        final Move move = new Move("D2/-12/0.103");
        assertEquals("D2/-12.00/0.103", move.toString());

        final Move move2 = new Move(new MoveScore("D2", -12 * CoefficientCalculator.DISK_VALUE), 103 * 0.001);
        assertEquals("D2/-12.00/0.103", move2.toString());

        final Move move3 = new Move(new MoveScore("D2", -12 * CoefficientCalculator.DISK_VALUE), 1);
        assertEquals("D2/-12.00/1.00", move3.toString());

        // these are taken from games downloaded from GGS to try to decode the format exactly
        testToString("a3//19.10");

        testToString("pa");
        testToString("pass");
        // not currently handling 10x10 games.
//        testToString("j4//26.22"); // from a 10x10 game

    }

    private void testToString(String s) {
        final Move move = new Move(s);
        assertEquals(s, move.toString());
    }

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
        assertEquals(sq, new Move(moveText).getSq());
    }
}
