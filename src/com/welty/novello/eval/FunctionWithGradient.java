package com.welty.novello.eval;

/**
 */
public interface FunctionWithGradient {
    /**
     * Calculate the direction of steepest descent (negative of the gradient)
     *
     * For each index i, the gradient is the partial derivative df/dx[i] evaluated at x[i].
     *
     * @param x location to evaluate gradient
     * @return negative of gradient
     */
    double[] minusGradient(double[] x);

    /**
     * Evaluate the function
     *
     * @param x location to evaluate function
     * @return function value
     */
    double y(double[] x);

    /**
     * @return  Number of dimensions in the function's input
     */
    int nDimensions();
}
