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

package com.welty.novello.coca;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;
import junit.framework.TestCase;

import static org.junit.Assert.assertArrayEquals;

/**
 */
public class ConjugateGradientMethodTest extends TestCase {
    /**
     * Test function minimization.
     *
     * This is a very simple example, taken from <a href="http://en.wikipedia.org/wiki/Conjugate_gradient_method#Numerical_example">Wikipedia</a>
     * @throws Exception
     */
    public void testMinimize() throws Exception {
        final double[][] A = {
                {4,1},
                {1,3}
        };
        final double[] root = {1./11, 7./11};

        FunctionWithGradient f = new QForm(A, root);
        final double[] minimize = ConjugateGradientMethod.minimize(f, new double[]{2, 1});
        assertArrayEquals(root, minimize, 1e-10);
    }

    /**
     * A Quadratic form.
     *
     * Given a root in R^n and an nxn array A, this is 1/2 (x-root)<sup>T</sup>A(x-root).
     */
    public static class QForm extends FunctionWithGradient {
        private final double[][] a;
        private final double[] root;

        public QForm(double[][] a, double[] root) {
            Require.square(a, "a");
            Require.eqLength(a, root);
            this.a = a;
            this.root = root;
        }

        @Override public double[] minusGradient(double[] x) {
            final double[] result = new double[x.length];
            final double[] dx = Vec.minus(x, root);
            for (int i=0; i<x.length; i++) {
                result[i] = -Vec.dot(a[i], dx);
            }
            return result;
        }

        @Override public double y(double[] x) {
            final double[] dx = Vec.minus(x, root);
            double result = 0;
            for (int i=0; i<dx.length; i++) {
                result += dx[i]*Vec.dot(a[i],dx);
            }
            return .5*result;
        }

        @Override public int nDimensions() {
            return root.length;
        }
    }
}
