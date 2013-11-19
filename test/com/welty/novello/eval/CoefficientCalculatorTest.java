package com.welty.novello.eval;

import com.orbanova.common.misc.ArrayTestCase;

/**
 */
public class CoefficientCalculatorTest extends ArrayTestCase {
    public void testCalc() {

        final Element[] elements = {
                new Element(new int[]{2, 1, 5, 4}, 8)
        };

        final double[] coeffs = CoefficientCalculator.estimateCoefficients(elements, 6);
        assertEquals(new double[]{0., 2., 2., 0, 2., 2.}, coeffs, 1e-10);
    }
}
