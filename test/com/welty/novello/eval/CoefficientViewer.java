package com.welty.novello.eval;

/**
 */
public class CoefficientViewer {
    public static void main(String[] args) {
        final EvalStrategy strategy = EvalStrategies.eval4;
        final String coeffSetName = "C";

        for (int nEmpty = 10; nEmpty <= 50; nEmpty+=10) {
            final int[][] slice = strategy.readSlice(nEmpty, coeffSetName);
            System.out.println();
            System.out.println("=== Coefficients for " + strategy + " with " + nEmpty + " empties ===");
            strategy.dumpCoefficients(slice);
        }
    }
}
