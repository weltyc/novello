/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.solver;

import com.orbanova.common.misc.Vec;

/**
 */
class NodeCounts {
    private final long[] nNodes = new long[64];
    private final long[][] nNodesByDepthAndType = new long[64][3];
    private final long[][] nNodesByCutIndex = new long[64][64];

    /**
     * @return number of nodes since application startup. Passes do not count as nodes.
     */
    long getNNodes() {
        return Vec.sum(nNodes);
    }

    String getNodeCountsByDepth() {
        final double N = getNNodes() * .01;
        StringBuilder sb = new StringBuilder();
        sb.append("\nNode fractions by depth:\n");
        for (int i = 1; i < 64; i++) {
            final long nNodes = this.nNodes[i];
            if (nNodes > 0) {
                appendNodePercent(sb, i, nNodes, N);
                appendAverageCut(sb, i);
                sb.append('\n');
            }
        }
        sb.append("\nPredicted node type distribution:\n");
        for (int i = 1; i < 64; i++) {
            appendNodeTypes(sb, i);
        }
        return sb.toString();
    }

    private void appendNodeTypes(StringBuilder sb, int d) {
        final long[] byType = nNodesByDepthAndType[d];
        final long total = Vec.sum(byType);
        if (total > 0) {
            final double N = total * 0.01;
            sb.append(String.format("[%2d] ALL %2.0f%%, PV %2.0f%%, CUT %2.0f%% %n", d, byType[0] / N, byType[1] / N, byType[2] / N));
        }
    }

    private static void appendNodePercent(StringBuilder sb, int nEmpties, long nNodes, double N) {
        if (nNodes > 0) {
            final double frac = nNodes / N;
            final String format = frac >= 1 ? "%2d: %4.1f%%" : "%2d:  %4.2g%%";
            final String np = String.format(format, nEmpties, frac);
            sb.append(String.format("%-16s", np));
        }
    }

    private void appendAverageCut(StringBuilder sb, int nEmpties) {
        final long[] ci = nNodesByCutIndex[nEmpties];
        final long nTotal = Vec.sum(ci);
        long nWasted = 0;
        for (int i = 0; i < ci.length; i++) {
            nWasted += i * ci[i];
        }
        if (nTotal != 0) {
            sb.append(String.format("First cut: %2d%%. Wasted: %4.2f", 100 * ci[0] / nTotal, (double) nWasted / nTotal));
        }
    }

    /**
     * Update the stats with a node of a given type
     *
     * @param nEmpties # of empty squares at root position
     * @param nodeType PRED_ALL, PRED_PV, PRED_CUT
     */
    public void update(int nEmpties, int nodeType) {
        nNodes[nEmpties]++;
        nNodesByDepthAndType[nEmpties][nodeType + 1]++;
    }

    /**
     * Update the stats with the nodeIndex of a cut
     *
     * @param nEmpties number of empties in the parent position
     * @param nodeIndex 0 for first node, 1 for second node, etc.
     */
    public void updateCut(int nEmpties, int nodeIndex) {
        nNodesByCutIndex[nEmpties][nodeIndex]++;
    }

    /**
     * Update the stats with a node of unknown type
     * <p/>
     * Type information is typically not available at low depths.
     *
     * @param nEmpties # of empty squares at root position
     */
    public void update(int nEmpties) {
        nNodes[nEmpties]++;
    }
}
