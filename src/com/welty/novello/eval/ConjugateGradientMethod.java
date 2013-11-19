package com.welty.novello.eval;

import com.orbanova.common.math.function.oned.Function;
import com.orbanova.common.math.function.oned.Functions;
import com.orbanova.common.math.function.oned.Optimum;
import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;

/**
 * Minimize a function: R<sup>n</sup> &rarr; R using the conjugate gradient method.
 *
 * algorithm copied from http://en.wikipedia.org/wiki/Nonlinear_conjugate_gradient_method
 */
public class ConjugateGradientMethod {

    /**
     * Return the location of an approximate minimum of f
     *
     * @param f  function to minimize
     * @return approximate minimum location
     */
    static double[] minimize(FunctionWithGradient f) {
        final double[] x = new double[f.nDimensions()];
        return minimize(f, x);
    }

    /**
     * Return the location of an approximate minimum of f
     *
     * @param f  function to minimize
     * @return approximate minimum location. Note, this will be overwritten with the result.
     */
    static double[] minimize(FunctionWithGradient f, double[] x) {
        final double[] deltaX0 = f.minusGradient(x);

        final double alpha0 = argmin(f, x, deltaX0);
        Vec.plusTimesEquals(x, deltaX0, alpha0);
        double[] s = deltaX0;
        double deltaXPrev[] = deltaX0;

        for (int i = 0; i < 10; i++) {
            final double[] deltaXn = f.minusGradient(x);
            if (Vec.isZero(deltaXn)) {
                // we're at a minimum!
                break;
            }
            //noinspection SuspiciousNameCombination
            final double beta = Vec.dot(deltaXn, deltaXn) / Vec.dot(deltaXPrev, deltaXPrev); // Fletcher-Reeves
            s = Vec.plusTimes(deltaXn, s, beta);
            final double alpha = argmin(f, x, s);
            Vec.plusTimesEquals(x, s, alpha);
            deltaXPrev = deltaXn;
        }
        return x;
    }

    private static double argmin(FunctionWithGradient f, double[] x0, double[] deltaX0) {
        Require.normalized(deltaX0);
        Require.normalized(x0);
        final Function line = new LineFunction(f, x0, deltaX0);
        final Optimum optimum = Functions.minimize(line, 0, 1);
        return optimum.getX();
    }

    private static class LineFunction implements Function {
        private final FunctionWithGradient f;
        private final double[] x0;
        private final double[] deltaX0;

        public LineFunction(FunctionWithGradient f, double[] x0, double[] deltaX0) {
            this.f = f;
            this.x0 = x0;
            this.deltaX0 = deltaX0;
        }

        @Override public double y(double alpha) {
            return f.y(Vec.plusTimes(x0, deltaX0, alpha));
        }
    }
}
