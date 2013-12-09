package com.welty.novello.solver;

/**
 */
public class StableStatistics {
    final long[] counts = new long[65];
    long alphaCuts;
    long betaCuts;
    long fails;
    long uncalculated;

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("## Stable disk counts at 6 empty ##\n");
        for (int i = 0; i <= 64; i++) {
            if (counts[i] > 0) {
                sb.append(String.format("%2d: %,9d\n", i, counts[i]));
            }
        }
        final long totalNodes = alphaCuts + betaCuts + fails + uncalculated;
        final double pctBeta = 100. * betaCuts / totalNodes;
        final double pctAlpha = 100. * alphaCuts / totalNodes;
        final double pctCalculated = 100 - 100. * uncalculated / totalNodes;
        sb.append(String.format("stable beta cuts: %.1f%%, alpha cuts: %.1f%%. %3.1f%% received full stability calc out of %,d total nodes\n", pctBeta, pctAlpha, pctCalculated, totalNodes));

        return sb.toString();
    }
}
