package com.welty.novello.eval;

/**
 */
public class CoefficientViewer {
    public static void main(String[] args) {
        final EvalStrategy strategy = EvalStrategies.eval2;
        final int nEmpty = 10;

        final int[][] slice = strategy.readSlice(nEmpty);
        System.out.println("=== Coefficients for " + strategy + " with " + nEmpty + " empties ===");
        strategy.dumpCoefficients(slice);
    }
}
