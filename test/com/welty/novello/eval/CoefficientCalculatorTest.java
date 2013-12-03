package com.welty.novello.eval;

import com.orbanova.common.misc.ArrayTestCase;
import com.orbanova.common.misc.Vec;

/**
 */
public class CoefficientCalculatorTest extends ArrayTestCase {
    public void testCalc() {

        final PositionElement[] elements = {
                new PositionElement(new int[]{2, 1, 5, 4}, 8)
        };

        for (double penalty = 0; penalty <= 2; penalty++) {
            final double[] coeffs = CoefficientCalculator.estimateCoefficients(elements, 6, 0, penalty);
            final double x = 8 / (4 + penalty);
            assertEquals(new double[]{0., x, x, 0, x, x}, coeffs, 1e-8);
        }
    }

    public void testCoefficientErrorFunction() {
        // only one element, with a target of 8.
        // Each of the four coefficients should be 2; the other coefficients should be 0.
        final PositionElement[] elements = {
                new PositionElement(new int[]{2, 1, 5, 4}, 8)
        };
        // err from target when x=0.
        final double err = 8.;

        final CoefficientCalculator.ErrorFunction function = new CoefficientCalculator.ErrorFunction(elements, 6, 0, 0);
        assertEquals(6, function.nDimensions());
        final double[] x0 = {0, 0, 0, 0, 0, 0};
        assertEquals("sum of squared errors", err * err, function.y(x0), 1e-10);
        assertEquals("sum of squared errors", (12 - 8) * (12 - 8), function.y(new double[]{0, 1, 2, 3, 4, 5}), 1e-10);
        assertEquals("steepest descent", new double[]{0, 2 * err, 2 * err, 0, 2 * err, 2 * err}, function.minusGradient(x0), 1e-10);
    }

    public void testCoefficientErrorFunctionWithPenalty() {
        // only one element, with a target of 8.
        // All four coefficients are the same - call it x.
        // the error function is now (8-4x)^2 + penalty*x^2.
        // Its minimum occurs when 0 = d error / dx = (4+p)x -8
        // so x = 8/(4+p)
        final int nDimensions = 6;
        final PositionElement[] elements = {
                new PositionElement(new int[]{2, 1, 5, 4}, 8)
        };
        final double[] x0 = {0, 0, 0, 0, 0, 0};
        final double err0 = 8.;         // err from target when x=0.

        final double[] x1 = {0, 1, 2, 3, 4, 5};
        final double err1 = -4.;   // err from target when x=x1.

        for (double penalty = 0; penalty < 4; penalty++) {
            final CoefficientCalculator.ErrorFunction function = new CoefficientCalculator.ErrorFunction(elements, nDimensions, 0, penalty);

            // at x0
            assertEquals(nDimensions, function.nDimensions());
            assertEquals("sum of squared errors", err0 * err0, function.y(x0), 1e-10);
            assertEquals("steepest descent", new double[]{0, 2 * err0, 2 * err0, 0, 2 * err0, 2 * err0}, function.minusGradient(x0), 1e-10);

            // at x1
            assertEquals("sum of squared errors", err1 * err1 + penalty * Vec.sumSq(x1), function.y(x1), 1e-10);
            final double[] expectedNoPenalty = {0, 2 * err1, 2 * err1, 0, 2 * err1, 2 * err1};
            final double[] expected = Vec.plusTimes(expectedNoPenalty, x1, -2 * penalty);
            assertEquals("steepest descent", expected, function.minusGradient(x1), 1e-10);

            // line function
            final double[] dx = Vec.increasingDouble(1., 1., nDimensions);
            for (int a = 0; a < 3; a++) {
                final double[] xa = Vec.plusTimes(x1, dx, a);
                assertEquals(function.y(xa), function.getLineFunction(x1, dx).y(a));
            }
        }
    }

    public void testCoefficientErrorFunctionWithDenseOrid() {
        // One element with a dense coefficient of 1 and an orid of 0
        final PositionElement[] elements = {
                new PositionElement(new int[]{0}, 3, new float[]{1.0f})
        };

        final CoefficientCalculator.ErrorFunction function = new CoefficientCalculator.ErrorFunction(elements, 2, 1, 1e-12);
        assertEquals(3, function.nDimensions());
        final double[] x0 = {0., 0., 0.};
        final double err0 = 3;
        assertEquals(err0*err0, function.y(x0), 1e-10);
        assertEquals(new double[]{err0*2, 0, err0*2}, function.minusGradient(x0), 1e-10);

        final double[] x1 = {1., 0., 0.};
        final double err1 = 2;
        assertEquals(err1*err1, function.y(x1), 1e-10);
        assertEquals(new double[]{err1*2, 0, err1*2}, function.minusGradient(x1), 1e-10);

        final double[] x3 = {0. , 0., 1.};
        final double err3 = 2;
        assertEquals(err3*err3, function.y(x1), 1e-10);
        assertEquals(new double[]{err3*2, 0, err3*2}, function.minusGradient(x3), 1e-10);
    }

    public void testElementError() {
        final PositionElement element = new PositionElement(new int[]{0}, 3, new float[]{1.0f});

        final double[] x0 = {0., 0., 0.};
        assertEquals(3, element.error(x0), 1e-10);
        assertEquals(0, element.dError(x0), 1e-10);

        final double[] x1 = {1., 0., 0.};
        assertEquals(2, element.error(x1), 1e-10);
        assertEquals(-1, element.dError(x1), 1e-10);

        final double[] x2 = {0., 1., 0.};
        assertEquals(3, element.error(x2), 1e-10);
        assertEquals(0, element.dError(x2), 1e-10);

        final double[] x3 = {0., 0., 1.};
        assertEquals(2, element.error(x3), 1e-10);
        assertEquals(-1, element.dError(x3), 1e-10);
    }
}
