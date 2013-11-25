package com.welty.novello.eval;

/**
 */
public class CoefficientViewer {
    public static void main(String[] args) {
        final int nEmpty = 24;

        dumpSlice("6A", nEmpty);
        dumpSlice("5K", nEmpty);
    }

    private static void dumpSlice(String eval, int nEmpty) {
        final EvalStrategy strategy = EvalStrategies.strategy(eval.substring(0, 1));
        final String coeffSetName = eval.substring(1);
        final int[][] slice = strategy.readSlice(nEmpty, coeffSetName);
        System.out.println();
        System.out.println("=== Coefficients for " + strategy + coeffSetName + " with " + nEmpty + " empties ===");
        strategy.dumpCoefficients(slice);
    }
}
