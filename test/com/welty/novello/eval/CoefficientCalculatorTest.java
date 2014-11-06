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

package com.welty.novello.eval;

import com.orbanova.common.misc.Vec;
import com.welty.novello.coca.RarePositionMrSource;
import com.welty.novello.core.Me;
import com.welty.novello.core.MeValue;
import com.welty.novello.core.MinimalReflection;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;

/**
 */
public class CoefficientCalculatorTest extends TestCase {
    public void testCalc() {

        final PositionElement[] elements = {
                new PositionElement(new int[]{2, 1, 5, 4}, 8)
        };

        for (double penalty = 0; penalty <= 2; penalty++) {
            final double[] coeffs = CoefficientCalculator.estimateCoefficients(elements, 6, 0, penalty);
            final double x = 8 / (4 + penalty);
            assertArrayEquals(new double[]{0., x, x, 0, x, x}, coeffs, 1e-8);
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
        assertArrayEquals("steepest descent", new double[]{0, 2 * err, 2 * err, 0, 2 * err, 2 * err}, function.minusGradient(x0), 1e-10);
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
            assertArrayEquals("steepest descent", new double[]{0, 2 * err0, 2 * err0, 0, 2 * err0, 2 * err0}, function.minusGradient(x0), 1e-10);

            // at x1
            assertEquals("sum of squared errors", err1 * err1 + penalty * Vec.sumSq(x1), function.y(x1), 1e-10);
            final double[] expectedNoPenalty = {0, 2 * err1, 2 * err1, 0, 2 * err1, 2 * err1};
            final double[] expected = Vec.plusTimes(expectedNoPenalty, x1, -2 * penalty);
            assertArrayEquals("steepest descent", expected, function.minusGradient(x1), 1e-10);

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
        assertArrayEquals(new double[]{err0*2, 0, err0*2}, function.minusGradient(x0), 1e-10);

        final double[] x1 = {1., 0., 0.};
        final double err1 = 2;
        assertEquals(err1*err1, function.y(x1), 1e-10);
        assertArrayEquals(new double[]{err1*2, 0, err1*2}, function.minusGradient(x1), 1e-10);

        final double[] x3 = {0. , 0., 1.};
        final double err3 = 2;
        assertEquals(err3*err3, function.y(x1), 1e-10);
        assertArrayEquals(new double[]{err3*2, 0, err3*2}, function.minusGradient(x3), 1e-10);
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

    public void testDenseCoefficientEstimation() {
        final Feature denseFeature = new DenseFeature() {

            @Override public float denseWeight(int orid) {
                return (float)Math.sqrt(1+orid);
            }

            @Override public int nOrids() {
                return 2;
            }

            @Override public String oridDescription(int orid) {
                return null;
            }

            @Override public int nInstances() {
                return 2;
            }

            @Override public int orid(int instance) {
                return instance;
            }
        };

        Term denseTerm = new Term(denseFeature) {
            @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
                return Long.bitCount(mover)&1;
            }
        };

        final EvalStrategy strategy = new EvalStrategy("denseTest", denseTerm);
        // with these two elements we can get a perfect fit
        final PositionElement[] elements = {
                new PositionElement(new int[]{0}, 5, new float[]{1f}),
                new PositionElement(new int[]{1}, 3, new float[]{1.414f}),
        };

        final double[] x = CoefficientCalculator.estimateCoefficients(elements, 2, 1, 0);
        final double[] coeffs = strategy.unpack(x);
        assertArrayEquals(new double[]{5., 3.}, coeffs, 1e-3);
    }

    public void testGenerateRareSubpositions() {
        final Me me = Me.early;
        final MeValue pv = new MeValue(me.mover, me.enemy, 13);

        // check with 1 position in pv
        final Set<MinimalReflection> subs = RarePositionMrSource.generateRareSubpositions(EvalStrategies.eval1, Arrays.asList(pv));
        final int nMoves = Long.bitCount(me.calcMoves());
        assertEquals(nMoves, subs.size());

        // check with 2 positions in pv, one of which will also be a subposition
        final MinimalReflection me2 = subs.iterator().next();
        final MeValue pv2 = new MeValue(me2.mover, me2.enemy, -13);
        final Set<MinimalReflection> subs2 = RarePositionMrSource.generateRareSubpositions(EvalStrategies.eval1, Arrays.asList(pv, pv2));
        final int nMoves2 = Long.bitCount(me2.calcMoves());
        assertEquals(nMoves + nMoves2 - 1, subs2.size());
    }
}
