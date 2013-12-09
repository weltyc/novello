package com.welty.novello.solver;

import com.welty.novello.eval.CoefficientCalculator;

/**
 */
public class CutoffStatistics {
    private final Statistic[] aboveBeta = createStatistics(100);
    private final Statistic[] belowAlpha = createStatistics(100);
    private final Statistic pvCutoffs = new Statistic();
    private final Statistic[] predictedType = createStatistics(3);

    private static Statistic[] createStatistics(int length) {
        final Statistic[] statistics = new Statistic[length];
        for (int i = 0; i < length; i++) {
            statistics[i] = new Statistic();
        }
        return statistics;
    }

    private void collectStatistics(long mover, long enemy, int alpha, int beta, int score, int nodeType) {
//        if (nodeType != Solver.PRED_ALL) {
//            return;
//        }
        // todo will need to get eval somehow
//        final int eval = MoveSorter.sortEval.eval(mover, enemy) / CoefficientCalculator.DISK_VALUE;
//        final Statistic statistic;
//
//        if (eval >= beta) {
//            statistic = aboveBeta[(eval - beta) / 4];
//        } else if (eval <= alpha) {
//            statistic = belowAlpha[(alpha - eval) / 4];
//        } else {
//            statistic = pvCutoffs;
//        }
//        statistic.update(score, alpha, beta);
//
//        predictedType[nodeType + 1].update(score, alpha, beta);
//
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();

        long totalNodes = 0;

        for (int margin = belowAlpha.length; margin-- > 0; ) {
            final Statistic statistic = belowAlpha[margin];
            statistic.append(sb, "<? " + String.format("%2d-%2d", margin * 4, margin * 4 + 3));
            totalNodes += statistic.nChances();
        }
        pvCutoffs.append(sb, "pv      ");
        totalNodes += pvCutoffs.nChances();
        for (int margin = 0; margin < aboveBeta.length; margin++) {
            final Statistic statistic = aboveBeta[margin];
            statistic.append(sb, ">ß " + String.format("%2d-%2d", margin * 4, margin * 4 + 3));
            totalNodes += statistic.nChances();
        }
        System.out.println();


        final String[] predictedTypeNames = {"PRED_ALL", "PRED_PV ", "PRED_CUT"};
        for (int i = 0; i < predictedType.length; i++) {
            predictedType[i].append(sb, predictedTypeNames[i]);
        }

        sb.append('\n');
        sb.append(String.format("Statistics calculated for %,d total nodes\n", totalNodes));
        return sb.toString();
    }

    private final static class Statistic {
        private long nCutoffs;
        private long nImprovements;
        private long nFails;

        void update(int score, int alpha, int beta) {
            if (score >= beta) {
                nCutoffs++;
            } else if (score <= alpha) {
                nFails++;
            } else {
                nImprovements++;
            }
        }

        long nChances() {
            return nCutoffs + nImprovements + nFails;
        }

        @Override public String toString() {
            final long nChances = nChances();
            return String.format("%3d%% CUT, %3d%% PV, %3d%% ALL   %,9d nodes", percent(nCutoffs)
                    , percent(nImprovements), percent(nFails), nChances);
        }

        public void append(StringBuilder sb, String description) {
            if (nChances() > 0) {
                sb.append(description).append(": ").append(toString()).append('\n');
            }
        }

        private int percent(long numerator) {
            return Math.round((float) numerator * 100f / nChances());
        }
    }
}
