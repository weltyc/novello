package com.welty.novello.eval;

import junit.framework.TestCase;

/**
 */
public class LinePatternFeatureTest extends TestCase {
    @SuppressWarnings("PointlessArithmeticExpression")
    public void testReverse() {
        assertEquals(0, LinePatternFeatureFactory.reverse(0, 1));
        assertEquals(1, LinePatternFeatureFactory.reverse(1, 1));
        assertEquals(2, LinePatternFeatureFactory.reverse(2, 1));
        assertEquals("12", 2*3 + 1, LinePatternFeatureFactory.reverse(1*3 + 2, 2));
        assertEquals("0120", 2*3*3 + 1*3, LinePatternFeatureFactory.reverse(1*3*3 + 2*3, 4));
    }

    public void testNOrids() {
        assertEquals(3, LinePatternFeatureFactory.of(1).nOrids());
        assertEquals((9+3)/2, LinePatternFeatureFactory.of(2).nOrids());
        assertEquals((27+9)/2, LinePatternFeatureFactory.of(3).nOrids());
        assertEquals((81+9)/2, LinePatternFeatureFactory.of(4).nOrids());
        assertEquals((243+27)/2, LinePatternFeatureFactory.of(5).nOrids());
    }

    public void testOridDescription() {
        testOridDescription(1, 0, ".");
        testOridDescription(1, 1, "*");
        testOridDescription(1, 2, "O");
        testOridDescription(2, 1, ".*");
        // just after first duplicate orid
        testOridDescription(2, 3, "**");
    }

    private static void testOridDescription(int nDisks, int orid, String expected) {
        assertEquals(expected, LinePatternFeatureFactory.of(nDisks).oridDescription(orid));
    }

    public void testNInstances() {
        assertEquals(3, LinePatternFeatureFactory.of(1).nInstances());
        assertEquals(9, LinePatternFeatureFactory.of(2).nInstances());
        assertEquals(27, LinePatternFeatureFactory.of(3).nInstances());
        assertEquals(81, LinePatternFeatureFactory.of(4).nInstances());
    }

    public void testOrid() {
        testOrid(1, 0, 0);
        testOrid(1, 1, 1);
        testOrid(1, 2, 2);

        testOrid(2, 0, 0);
        testOrid(2, 1, 1);
        testOrid(2, 2, 2);
        testOrid(2, 3, 1);
        testOrid(2, 4, 3);
        testOrid(2, 5, 4);
        testOrid(2, 6, 2);
        testOrid(2, 7, 4);
        testOrid(2, 8, 5);
    }

    private void testOrid(int nDisks, int instance, int orid) {
        assertEquals(orid, LinePatternFeatureFactory.of(nDisks).orid(instance));
    }
}
