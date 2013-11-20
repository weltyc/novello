package com.welty.novello.eval;

import com.orbanova.common.math.function.oned.Function;
import com.orbanova.common.math.function.oned.Functions;
import com.orbanova.common.math.function.oned.Optimum;
import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;
import org.jetbrains.annotations.NotNull;

/**
 */
public abstract class FunctionWithGradient {
    /**
     * Calculate the direction of steepest descent (negative of the gradient)
     * <p/>
     * For each index i, the gradient is the partial derivative df/dx[i] evaluated at x[i].
     *
     * @param x location to evaluate gradient
     * @return negative of gradient
     */
    public abstract double[] minusGradient(double[] x);

    /**
     * Evaluate the function
     *
     * @param x location to evaluate function
     * @return function value
     */
    public abstract double y(double[] x);

    /**
     * @return Number of dimensions in the function's input
     */
    public abstract int nDimensions();

    /**
     * Minimize the function along a line
     * <p/>
     * This searches over values of a to find the minimum value of f(x + a*dx).
     *
     * @param x  initial point on the line
     * @param dx direction of the line
     * @return An Optimum containing (a, f(x+a*da)) at the minimum
     */
    Optimum findOptimum(double[] x, double[] dx) {
        Require.finite(dx);
        Require.finite(x);
        final Function line = getLineFunction(x, dx);
        return Functions.minimize(line, 0, 1);
    }

    /**
     * Create a function whose values are this Function's values along a line
     * @param x  initial point on the line
     * @param dx direction of the line
     * @return a function: a -> this.y(x + a * dx)
     */
    protected @NotNull Function getLineFunction(double[] x, double[] dx) {
        return new LineFunction(this, x, dx);
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
