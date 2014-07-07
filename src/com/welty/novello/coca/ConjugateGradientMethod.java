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

import com.orbanova.common.math.function.oned.Optimum;
import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Vec;

/**
 * Minimize a function: R<sup>n</sup> &rarr; R using the conjugate gradient method.
 * <p/>
 * algorithm copied from http://en.wikipedia.org/wiki/Nonlinear_conjugate_gradient_method
 */
public class ConjugateGradientMethod {
    private static final Logger log = Logger.logger(ConjugateGradientMethod.class);

    /**
     * Return the location of an approximate minimum of f
     *
     * @param f function to minimize
     * @return approximate minimum location
     */
    public static double[] minimize(FunctionWithGradient f) {
        final double[] x = new double[f.nDimensions()];
        return minimize(f, x);
    }

    /**
     * Return the location of an approximate minimum of f
     *
     * @param f function to minimize
     * @return approximate minimum location. Note, this will be overwritten with the result.
     */
    public static double[] minimize(FunctionWithGradient f, double[] x) {
        // reset directions every so often because of nonlinearity and roundoff error
        for (int resetSize = 2; resetSize <= 1024; resetSize *= 2) {
            final double[] deltaX0 = f.minusGradient(x);
            final Optimum optimum1 = f.findOptimum(x, deltaX0);
            double prevFx = optimum1.getFx();
            final double alpha0 = optimum1.getX();
            Vec.plusTimesEquals(x, deltaX0, alpha0);
            double[] s = deltaX0;
            double deltaXPrev[] = deltaX0;

            for (int i = 0; i < resetSize; i++) {
                final double[] deltaXn = f.minusGradient(x);
                if (Vec.isZero(deltaXn)) {
                    // we're at a minimum!
                    break;
                }
                //noinspection SuspiciousNameCombination
                final double beta = Vec.dot(deltaXn, deltaXn) / Vec.dot(deltaXPrev, deltaXPrev); // Fletcher-Reeves
                s = Vec.plusTimes(deltaXn, s, beta);
                final Optimum optimum = f.findOptimum(x, s);
                final double alpha = optimum.getX();
                Vec.plusTimesEquals(x, s, alpha);
                deltaXPrev = deltaXn;

                // check for stopping criterion
                double fx = optimum.getFx();
                final double improvement = prevFx - fx;
//                System.out.format("improvement: %.4g to %.4g%n", improvement, fx);
                if (improvement < 0) {
                    log.warn("improvement was negative");
                }
                if (improvement < 1e-5 * Math.abs(fx)) {
                    // we're done!
                    return x;
                }
                prevFx = fx;
            }
        }
        log.warn("might not have converged");
        return x;
    }

}
