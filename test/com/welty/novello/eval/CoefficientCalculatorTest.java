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
