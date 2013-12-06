package com.welty.novello.eval;

/**
 */
public class CoefficientViewer {
    public static void main(String[] args) {
        final int nEmpty = 50;

        dumpSlice("8A", nEmpty, 0);
    }

    /**
     * Print out all coefficients with absolute value >= minValue
     */
    private static void dumpSlice(String eval, int nEmpty, int minValue) {
        final EvalStrategy strategy = EvalStrategies.strategy(eval.substring(0, 1));
        final String coeffSetName = eval.substring(1);
        final short[][] slice = strategy.readSlice(nEmpty, coeffSetName);
        System.out.println();
        System.out.println("=== Coefficients for " + strategy + coeffSetName + " with " + nEmpty + " empties ===");
        strategy.dumpCoefficients(slice, minValue);
    }
}
