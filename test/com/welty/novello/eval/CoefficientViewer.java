package com.welty.novello.eval;

/**
 */
public class CoefficientViewer {
    public static void main(String[] args) {
        final EvalStrategy strategy = CoefficientCalculator.STRATEGY;
        final String coeffSetName = CoefficientCalculator.COEFF_SET_NAME;

        final int nEmpty = 31;

        final int[][] slice = strategy.readSlice(nEmpty, coeffSetName);
        System.out.println("=== Coefficients for " + strategy + " with " + nEmpty + " empties ===");
        strategy.dumpCoefficients(slice);
    }
}
