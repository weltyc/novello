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

package com.welty.novello.eval;

import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Require;
import com.welty.novello.core.NovelloUtils;
import com.welty.novello.solver.MidgameSearcher;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Mpc {
    private static final Logger log = Logger.logger(Mpc.class);

    private final Slice[] slices = new Slice[64];

    Mpc(ArrayList<int[]>[] sliceData) {
        log.debug("creating Mpc");
        Require.eqLength(sliceData, slices);
        for (int nEmpty = 0; nEmpty < slices.length; nEmpty++) {
            slices[nEmpty] = new Slice(nEmpty, sliceData[nEmpty]);
        }
    }

    /**
     * Create an Mpc with no data at all, just the formula approximation
     */
    Mpc() {
        log.debug("creating Mpc");
        for (int nEmpty = 0; nEmpty < slices.length; nEmpty++) {
            slices[nEmpty] = new Slice(nEmpty, new ArrayList<int[]>());
        }
    }

    private static int readInt(String line, int beginIndex, int endIndex) {
        return Integer.parseInt(line.substring(beginIndex, endIndex).trim());
    }

    static ArrayList<int[]>[] readSliceData(Path path) throws IOException {
        //noinspection unchecked
        final ArrayList<int[]>[] sliceData = new ArrayList[64];
        for (int i = 0; i < sliceData.length; i++) {
            sliceData[i] = new ArrayList<>();
        }

        try (BufferedReader in = Files.newBufferedReader(path, Charset.defaultCharset())) {
            String line;
            while (null != (line = in.readLine())) {
                final int beginIndex = 0;
                final int endIndex = 2;
                final int nEmpty = readInt(line, beginIndex, endIndex);
                final int n = (line.length() - 2) / 6;
                int[] values = new int[n];
                for (int i = 0; i < n; i++) {
                    try {
                        values[i] = readInt(line, 2 + 6 * i, 8 + 6 * i);
                    } catch (NumberFormatException e) {
                        System.out.println("line : " + line);
                        throw (e);
                    }
                }
                sliceData[nEmpty].add(values);
            }
        }
        return sliceData;
    }

    /**
     * Get the Cutters for a search depth
     *
     * @param depth search depth
     * @return all available Cutters at that depth. May have length 0.
     */
    public @NotNull Mpc.Cutter[] cutters(int nEmpty, int depth) {
        return slices[nEmpty].cutters[depth];
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            sb.append("==== ").append(nEmpty).append(" empty =======\n");
            sb.append(slices[nEmpty]);
        }
        return sb.toString();
    }

    static final int[][] cutDepths = {
            {}, {}, // depth 0-1
            {0}, {1}, // depth 2-3
            {2}, {3}, // depth 4-5
            {2}, {3}, // depth 6-7
            {4}, {5}, // depth 8-9
            {4}, {5}, // depth 10-11
            {4}, {5}, // depth 12-13
            {4}, {5}, // depth 14-15
            {6}, {7}, // depth 16-17
            {6}, {7}, // depth 18-19
            {8}, {9}, // depth 20-21
            {8}, {9}, // depth 22-23
            {10}, {11}, // depth 24-25
            {10}, {11}, // depth 26-27
            {12}, {13}, // depth 28-29
            {12}, {13}, // depth 30-31
            {12}, {13}, // depth 32-33
            {14}, {15}, // depth 34-35
            {14}, {15}, // depth 36-37
            {14}, {15}, // depth 38-39
            {16}, {17}, // depth 40-41
            {16}, {17}, // depth 42-43
            {16}, {17}, // depth 44-45
            {18}, {19}, // depth 46-47
            {18}, {19}, // depth 47-48
            {18}, {19}, // depth 48-49
            {20}, {21}, // depth 50-51
            {20}, {21}, // depth 52-53
            {20}, {21}, // depth 54-55
    };

    /**
     * MPC solve widths.
     *
     * widthStrings are calculated using the Excel formula 2*normsdist(width)-1
     */
    private static final String[] widthStrings = {"68%", "79%", "87%", "92%", "95%", "98%", "99%", "99.4%", "99.7%"};
    private static final double[] mpcWidths = { 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5, 2.75, 3.0};

    /**
     * Get the display String for an MPC width index
     *
     * @param width width index
     * @return display String (e.g. "60%")
     */
    public static String widthString(int width) {
        return widthStrings[width];
    }

    /**
     * Get the number of standard deviations used for an MPC width index.
     * @param width width index
     * @return number of standard deviations for that width
     */
    public static double widthSd(int width) {
        return mpcWidths[width];

    }
    /**
     * Get maximum allowable width index
     *
     * @return max width index
     */
    public static int maxWidth() {
        return widthStrings.length-1;
    }

    static class Slice {
        final Cutter[][] cutters = new Cutter[64][];

        public Slice(int nEmpty, List<int[]> ints) {
            final int maxDataDepth;
            if (ints.size() > 2) {
                maxDataDepth = ints.get(0).length - 1;
            } else {
                maxDataDepth = 0;
            }

            int depth = 0;
            for (; depth < cutDepths.length; depth++) {
                final int[] shallowDepths = cutDepths[depth];
                final int nPairs = shallowDepths.length;
                cutters[depth] = new Cutter[nPairs];
                for (int p = 0; p < nPairs; p++) {
                    final int shallow = shallowDepths[p];
                    final Cutter cutter;
                    if (depth <= maxDataDepth) {
                        cutter = new Cutter(ints, depth, shallow);
                    } else {
                        cutter = new Cutter(nEmpty, depth, shallow);
                    }

                    cutters[depth][p] = cutter;
                }
            }
            for (; depth < cutters.length; depth++) {
                cutters[depth] = new Cutter[0];
            }
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder();
            for (int depth = 0; depth < cutDepths.length; depth++) {
                final Cutter[] pairCutters = cutters[depth];
                final int[] shallowDepths = cutDepths[depth];
                for (int pair = 0; pair < pairCutters.length; pair++) {
                    final Cutter cutter = pairCutters[pair];
                    sb.append(String.format(" %d/%2d -> %s\n", shallowDepths[pair], depth, cutter));
                }
            }
            return sb.toString();
        }
    }

    public static class Cutter {
        private final double a;
        private final double b;
        private final double shallowSd;
        public final int shallowDepth;

        /**
         * Programmatic estimation of Cutter, when we have no data
         */
        public Cutter(int nEmpty, int depth, int shallow) {
            a = b = 1;
            shallowSd = approximateSd(nEmpty, depth, shallow);
            shallowDepth = shallow;
        }

        static double approximateSd(int nEmpty, int depth, int shallow) {
            // adjust for the fact that, if nEmpty - depth < solverStartDepth, the search
            // will be extended to the end of the game.

            if ((nEmpty - depth) < MidgameSearcher.SOLVER_START_DEPTH) {
                depth = nEmpty;
            }

            // approximate cut sd as
            // f(shallow) g(delta) h(nEmpty)
            // where delta = min(depth, nEmpty)-shallow
            final int delta = Math.min(depth, nEmpty) - shallow;
            if (delta <= 0) {
                return 0;
            }
            final double f = 1. / (.71 + .1 * shallow);
            final double[] gs = {.8, 1.0, 1.1, 1.16, 1.21, 1.25, 1.30, 1.35, 1.40, 1.45, 1.50, 1.55, 1.60, 1.65, 1.70, 1.75, 1.80, 1.85, 1.90, 1.95, 2.00, 2.05};
            final double g = gs[(delta - 1) / 2];
            double h;
            if (nEmpty >= 30) {
                h = .85;
            } else if (nEmpty >= 15) {
                h = 1.15 - .02 * (nEmpty - 15);
            } else {
                h = 1.15 - .02 * (15 - nEmpty);
            }
            // NTest's coefficients J are calculated differently than other coefficients
            // so we need a narrower sd.
            h *= (80 - nEmpty) / 17.5;
            // for coefficients other than J, use
            // h *= (80 - nEmpty) / 14.;

            return f * g * h * CoefficientCalculator.DISK_VALUE;
        }

        public Cutter(List<int[]> ints, int depth, int shallowDepth) {
            this.shallowDepth = shallowDepth;
            double xSum = 0;
            double ySum = 0;
            for (int[] scores : ints) {
                xSum += scores[shallowDepth];
                ySum += scores[depth];
            }
            final double xMean = xSum / ints.size();
            final double yMean = ySum / ints.size();

            double xx = 0;
            double xy = 0;
            double yy = 0;
            for (int[] scores : ints) {
                final double x = scores[shallowDepth] - xMean;
                final double y = scores[depth] - yMean;
                xx += x * x;
                xy += x * y;
                yy += y * y;
            }

            // now we have the prediction
            // score[depth]-yMean = xy/xx (score[shallowDepth-xMean) + error
            // E(error^2) = (yy - xy * xy / xx)/(N-2)
            //
            // turn it into the simpler-to use reverse prediction equation
            // score[shallowDepth] = a*score[depth]+b + shallowSd
            // E(shallowSd^2) = (a^2 * yy - xx)/(N-2)
            //
            a = xx / xy;
            b = xMean - a * yMean;
            shallowSd = Math.sqrt((a * a * yy - xx) / (ints.size() - 2));
        }

        /**
         * Reverse prediction.
         * <p/>
         * Tells the shallow score that would be needed to predict a deep score.
         *
         * @param deepScore deepScore being predicted
         * @return the shallowScore that predicts a score of deepScore for a deeper search
         */
        public int shallowScore(int deepScore) {
            return (int) (a * deepScore + b);
        }

        /**
         * Reverse prediction.
         * <p/>
         * Tells the shallow alpha that should be used as a cutoff for a deep alpha prediction.
         *
         * @param deepAlpha deepScore being predicted
         * @param width     mpc width index
         * @return the shallow alpha that, if cut off, predicts at least a 2/3 chance of a deep alpha cutoff
         */
        public int shallowAlpha(int deepAlpha, int width) {
            return deepAlpha == NovelloUtils.NO_MOVE ? NovelloUtils.NO_MOVE : (int) (a * deepAlpha + b - shallowSd*mpcWidths[width]);
        }

        public int shallowBeta(int deepBeta, int width) {
            return deepBeta == -NovelloUtils.NO_MOVE ? -NovelloUtils.NO_MOVE : (int) (a * deepBeta + b + shallowSd*mpcWidths[width]);
        }

        @Override public String toString() {
            return String.format("shallow = %3.1f * deep %+3.1f   +/- %3.1f", a, b / 100, shallowSd / 100);
        }

        public double getSd() {
            return shallowSd;
        }
    }

    public static final Mpc DEFAULT;

    static {
        @SuppressWarnings("unchecked")
        ArrayList<int[]>[] noData = new ArrayList[64];
        for (int i = 0; i < 64; i++) {
            noData[i] = new ArrayList<>();
        }
        DEFAULT = new Mpc(noData);
    }
}
