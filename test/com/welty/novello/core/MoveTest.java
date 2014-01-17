package com.welty.novello.core;

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
        assertEquals("D2/-12.00/1", move3.toString());
    }
}
