package com.welty.novello.eval;

import com.orbanova.common.misc.ArrayTestCase;
import com.welty.novello.solver.BitBoard;

/**
 */
public class Eval1Test extends ArrayTestCase {
    public void testIndicesFromPosition() throws Exception {
        final EvalStrategy ef = new EvalStrategy();

        // a sample position.
        // square 077 is mover, square 070 is enemy,
        // square 007 has mover access, square 000 has enemy access
        final long mover = 0x8001010101010100L;
        final long enemy = 0x0180808080808000L;
        System.out.println(new BitBoard(mover, enemy, true));
        final int[] expected = {2, 1, 5, 4};
        assertEquals(expected, ef.oridsFromPosition(mover, enemy));
    }

    public void testCoefficientErrorFunction() {
        // only one element, with a target of 8.
        // Each of the four coefficients should be 2; the other coefficients should be 0.
        final Element[] elements = {
                new Element(new int[]{2, 1, 5, 4}, 8)
        };
        // err from target when x=0.
        final double err = 8.;

        final CoefficientCalculator.ErrorFunction function = new CoefficientCalculator.ErrorFunction(elements, 6);
        assertEquals(6, function.nDimensions());
        final double[] x0 = {0, 0, 0, 0, 0, 0};
        assertEquals("sum of squared errors", err*err, function.y(x0), 1e-10);
        assertEquals("sum of squared errors", (12 - 8) * (12 - 8), function.y(new double[]{0, 1, 2, 3, 4, 5}), 1e-10);
        assertEquals("steepest descent", new double[]{0, 2*err, 2*err, 0, 2*err, 2*err}, function.minusGradient(x0), 1e-10);
    }
}
