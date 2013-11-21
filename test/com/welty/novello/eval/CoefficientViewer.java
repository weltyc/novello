package com.welty.novello.eval;

/**
 */
public class CoefficientViewer {
    public static void main(String[] args) {
        final EvalStrategy strategy = EvalStrategies.current;
        final int nEmpty = 31;

        final int[][] slice = strategy.readSlice(nEmpty, "A");
        System.out.println("=== Coefficients for " + strategy + " with " + nEmpty + " empties ===");
        strategy.dumpCoefficients(slice);
    }
}
