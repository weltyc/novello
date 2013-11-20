package com.welty.novello.eval;

/**
 */
public class CoefficientViewer {
    public static void main(String[] args) {
        final EvalStrategy strategy = EvalStrategies.edgeEval;
        final int nEmpty = 12;

        final int[][] slice = strategy.readSlice(nEmpty);
        strategy.dumpCoefficients(slice);
    }
}
