package com.welty.novello.eval;

import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Require;
import com.welty.novello.core.NovelloUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Mpc {
    private static final Logger log = Logger.logger(Mpc.class);

    private final Slice[] slices = new Slice[64];

    /**
     * Create an Mpc with no cuts at all
     */
    private Mpc() {
        for (int nEmpty = 0; nEmpty < slices.length; nEmpty++) {
            slices[nEmpty] = new Slice(new ArrayList<int[]>());
        }
    }

    Mpc(ArrayList<int[]>[] sliceData) {
        log.debug("creating Mpc");
        Require.eqLength(sliceData, slices);
        for (int nEmpty = 0; nEmpty < slices.length; nEmpty++) {
            slices[nEmpty] = new Slice(sliceData[nEmpty]);
        }
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
            {}, // depth 0
            {}, // depth 1
            {0}, // depth 2
            {1}, // depth 3
            {2}, // depth 4
            {3}, // depth 5
            {2}, // depth 6
            {3}, // depth 7
            {4}, // depth 8
            {5}, // depth 9
    };

    private static class Slice {
        private final Cutter[][] cutters = new Cutter[64][];

        public Slice(ArrayList<int[]> ints) {
            if (ints.size() > 2) {
                final int maxDepth = Math.min(ints.get(0).length, cutDepths.length);

                for (int depth = 0; depth < maxDepth; depth++) {
                    final int[] shallowDepths = cutDepths[depth];
                    final int nPairs = shallowDepths.length;
                    cutters[depth] = new Cutter[nPairs];
                    for (int p = 0; p < nPairs; p++) {
                        cutters[depth][p] = new Cutter(ints, depth, shallowDepths[p]);
                    }
                }
                for (int depth = maxDepth; depth < cutters.length; depth++) {
                    cutters[depth] = new Cutter[0];
                }
            } else {
                for (int depth = 0; depth < cutDepths.length; depth++) {
                    cutters[depth] = new Cutter[0];
                }
            }

        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder();
            for (int depth = 0; depth < cutters.length; depth++) {
                final Cutter[] pairCutters = cutters[depth];
                final int[] shallowDepths = cutDepths[depth];
                for (int pair = 0; pair < pairCutters.length; pair++) {
                    final Cutter cutter = pairCutters[pair];
                    sb.append(String.format(" %d/%d -> %s\n", shallowDepths[pair], depth, cutter));
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

        public Cutter(ArrayList<int[]> ints, int depth, int shallowDepth) {
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
         * @return the shallow alpha that, if cut off, predicts at least a 2/3 chance of a deep alpha cutoff
         */
        public int shallowAlpha(int deepAlpha) {
            return deepAlpha == NovelloUtils.NO_MOVE ? NovelloUtils.NO_MOVE : (int) (a * deepAlpha + b - shallowSd);
        }

        public int shallowBeta(int deepBeta) {
            return deepBeta == -NovelloUtils.NO_MOVE ? -NovelloUtils.NO_MOVE : (int) (a * deepBeta + b + shallowSd);
        }

        @Override public String toString() {
            return String.format("shallow = %3.1f * deep %+3.1f   +/- %3.1f", a, b / 100, shallowSd / 100);
        }
    }

    public static final Mpc NULL = new Mpc();

}
