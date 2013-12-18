package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.NovelloUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Automatically tunes Solver parameters
 */
@SuppressWarnings("UnusedDeclaration")
public class SolverTuner {
    /**
     * Tune a single parameter.
     * <p/>
     * The timings are noisy. We get a Typical timing using 16 iterations.
     * In order to keep the parameters from wandering around randomly due to noise,
     * we only recommend altering a parameter value if lowMetric of the original is higher than highMetric of the new.
     * <p/>
     * If two sets of 16 timings are selected from identical distributions, the chance that
     * lowMetric of the original is higher than highMetric of the new by random chance is 0.60%.
     */
    public static void main(String[] args) {
        final int nEmpty = 24;

        final boolean tuneByNodes = true;

        if (!tuneByNodes) {
            DeepSolverTimer.warmUpHotSpot();
        }

//        for (int emptyBucket = 0; emptyBucket < 5; emptyBucket++) {
//            for (int evalBucket = 0; evalBucket < 6; evalBucket++) {
//                final Parameter sd = new SearchDepthParameter(emptyBucket, evalBucket);
//                if (sd.get() >= 0) {
//                    // ff parameter is ignored if sd < 0. Since this Tuner keeps going if there are ties,
//                    // it will run forever.
//                    final Parameter ff = new FastestFirstParameter(emptyBucket, evalBucket);
//                    final String ffTune = new SolverTuner(ff, new DeepSolverTimer(nEmpty), tuneByNodes).tune();
//                    results.append(ffTune);
//                }
//                final String sdTune = new SolverTuner(sd, new DeepSolverTimer(nEmpty), tuneByNodes).tune();
//                results.append(sdTune);
//            }
//        }
//
//        System.out.println(results);
        new SolverTuner(MIN_SORT_DEPTH, new DeepSolverTimer(20), true).tune();
        new SolverTuner(MOBILITY_WEIGHT, new DeepSolverTimer(20), true).tune();
        new SolverTuner(MOVER_POT_MOB_WEIGHT, new DeepSolverTimer(20), true).tune();
        new SolverTuner(ENEMY_POT_MOB_WEIGHT, new DeepSolverTimer(20), true).tune();
    }

    private final @NotNull Parameter parameter;
    private final boolean tuneByNodes;
    private final @NotNull Tunable tunable;
    private @NotNull Best best;

    /**
     * If tuneByNodes is true, we tune the parameter to produce the search with the smallest number of nodes.
     * If it is false, we tune the parameter to produce the search with the shortest time.
     * <p/>
     * Timing is more important but can be inconsistent between runs; thus the solver is run multiple times when
     * tuning by time. This takes longer.
     */
    public SolverTuner(@NotNull Parameter parameter, @NotNull Tunable tunable, boolean tuneByNodes) {
        this.parameter = parameter;
        this.tuneByNodes = tuneByNodes;
        this.tunable = tunable;
    }

    /**
     * Tune a parameter.
     *
     * @return sample code if the parameter has changed, or "" if not
     */
    private String tune() {
        final int originalValue = parameter.get();
        System.out.println("Tuning " + parameter);
        System.out.println("-- original value: " + originalValue + " --");
        final double metric = getMetric(false);
        best = new Best(metric, originalValue);

        tune(originalValue, +1);
        tune(originalValue, -1);

        parameter.set(originalValue);

        System.out.print("Recommended value of " + parameter + ": ");
        // set the parameter to its recommended value in preparation for further tuning.
        parameter.set(best.value);
        if (best.value == originalValue) {
            System.out.println("unchanged at " + originalValue);
            return "";
        } else {
            System.out.println("change from " + originalValue + " to " + best.value);
            return parameter + " = " + best.value + ";\n";
        }
    }

    /**
     * Get the value of the cost that we're tuning
     * <p/>
     * If the cost has a margin of error, for example in timings, this returns either an optimistic (low) value of the cost
     * or a pessimistic (high) value depending on the value of 'high'.
     * If the cost has no margin of error, for example node counts, this function
     * returns the same value for both low and high.
     *
     * @param high if true, return a pessimistic view of the cost
     * @return value of the cost with the currently set parameters
     */
    private double getMetric(boolean high) {
        final double metric;
        if (tuneByNodes) {
            metric = tunable.cost();
            System.out.println("Cost : " + NovelloUtils.engineeringLong((long) metric) + "$");
        } else {
            final Typical typical = Typical.timing(tunable);
            System.out.println(typical);
            metric = high ? typical.q3() : typical.q1();
        }
        return metric;
    }

    /**
     * @param originalValue original parameter value
     * @param dv            amount to add to parameter value each loop.
     */
    private void tune(int originalValue, int dv) {
        boolean ok = true;
        for (int proposedValue = originalValue + dv; ok && proposedValue >= parameter.min(); proposedValue += dv) {
            System.out.println("-- proposed value: " + proposedValue + " --");
            parameter.set(proposedValue);
            final double metric = getMetric(false);
            ok = best.update(metric, proposedValue);
        }
    }

    private static class Best {
        double time;
        int value;

        Best(double time, int value) {
            this.time = time;
            this.value = value;
        }

        /**
         * Check whether time is better than the current best time; if so, update this.
         * <p/>
         * This returns 'true' if the search should be continued. In order to allow for large flat spaces
         * in the search, it returns true if the cost is exactly equal to the old best cost.
         *
         * @param time  time of new run
         * @param value parameter value of new run
         * @return true if this was updated (new time was better) or new new time was equally good.
         */
        boolean update(double time, int value) {
            if (time < this.time) {
                this.time = time;
                this.value = value;
                System.out.println("Improved parameter value: " + value);
                return true;
            } else {
                return time < this.time;
            }
        }
    }

    private static abstract class Parameter {
        private final int min;

        protected Parameter(int min) {
            this.min = min;
        }

        abstract int get();

        abstract void set(int value);

        /**
         * @return Minimum legal value for the parameter
         */
        int min() {
            return min;
        }
    }

    private static class StaticFieldParameter extends Parameter {

        private final Field field;

        protected StaticFieldParameter(Class<?> clazz, String fieldName, int min) {
            super(min);
            try {
                this.field = clazz.getDeclaredField(fieldName);
                final int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers)) {
                    throw new IllegalArgumentException("field is not static");
                }
                if (field.getType() != int.class) {
                    throw new IllegalArgumentException("field is not an int");
                }
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override int get() {
            try {
                return field.getInt(null);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override void set(int value) {
            try {
                field.setInt(null, value);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override public String toString() {
            return field.getDeclaringClass().getSimpleName() + "." + field.getName();
        }
    }

    // MoveSorter parameters
    private static final Parameter MOBILITY_WEIGHT = new StaticFieldParameter(MoveSorter.class, "MOBILITY_WEIGHT", 0);
    private static final Parameter FIXED_ORDERING_WEIGHT = new StaticFieldParameter(MoveSorter.class, "FIXED_ORDERING_WEIGHT", 0);
    private static final Parameter ETC_WEIGHT = new StaticFieldParameter(MoveSorter.class, "ETC_WEIGHT", 0);
    private static final Parameter PARITY_WEIGHT = new StaticFieldParameter(MoveSorter.class, "PARITY_WEIGHT", 0);
    private static final Parameter DEEP_POT_MOB_WEIGHT = new StaticFieldParameter(MoveSorter.class, "DEEP_POT_MOB_WEIGHT", 0);
    private static final Parameter ENEMY_POT_MOB_WEIGHT = new StaticFieldParameter(MoveSorter.class, "ENEMY_POT_MOB_WEIGHT", 0);
    private static final Parameter MOVER_POT_MOB_WEIGHT = new StaticFieldParameter(MoveSorter.class, "MOVER_POT_MOB_WEIGHT", 0);
    private static final Parameter BETA_MARGIN = new StaticFieldParameter(MoveSorter.class, "BETA_MARGIN", 0);
    private static final Parameter MIN_EVAL_SORT_DEPTH = new StaticFieldParameter(MoveSorter.class, "MIN_EVAL_SORT_DEPTH", 0);
    private static final Parameter MIN_ETC_DEPTH = new StaticFieldParameter(MoveSorter.class, "MIN_ETC_DEPTH", Solver.MIN_HASH_DEPTH + 1);

    // Solver parameters
    private static final Parameter MIN_SORT_DEPTH = new StaticFieldParameter(Solver.class, "MIN_SORT_DEPTH", 5);
    private static final Parameter MIN_HASH_DEPTH = new StaticFieldParameter(Solver.class, "MIN_HASH_DEPTH", Solver.MIN_SORT_DEPTH);
    private static final Parameter MIN_NEGASCOUT_DEPTH = new StaticFieldParameter(Solver.class, "MIN_NEGASCOUT_DEPTH", Solver.MIN_SORT_DEPTH);

    private static class FixedWeightParameter extends Parameter {
        private final long squares;
        private final String squareType;

        protected FixedWeightParameter(long squares, String squareType) {
            super(0);
            this.squares = squares;
            this.squareType = squareType;
        }

        @Override int get() {
            final int sq = Long.numberOfTrailingZeros(squares);
            return FixedMoveOrdering.getValue(sq);
        }

        @Override void set(int value) {
            FixedMoveOrdering.setValue(squares, value);
        }

        @Override public String toString() {
            return "FixedMoveOrdering " + squareType;
        }
    }

    private static final Parameter CORNER_WEIGHT = new FixedWeightParameter(BitBoardUtils.CORNERS, "corner weight");
    private static final Parameter C_SQUARE_WEIGHT = new FixedWeightParameter(BitBoardUtils.C_SQUARES, "c-square weight");
    private static final Parameter X_SQUARE_WEIGHT = new FixedWeightParameter(BitBoardUtils.X_SQUARES, "x-square weight");

    private static class FastestFirstParameter extends Parameter {
        private final int emptyBucket;
        private final int evalBucket;

        public FastestFirstParameter(int emptyBucket, int evalBucket) {
            super(0);
            this.emptyBucket = emptyBucket;
            this.evalBucket = evalBucket;
        }

        @Override int get() {
            return MoveSorter.fastestFirsts[emptyBucket][evalBucket];
        }

        @Override void set(int value) {
            MoveSorter.fastestFirsts[emptyBucket][evalBucket] = value;
        }

        @Override public String toString() {
            return "fastestFirsts[" + emptyBucket + "][" + evalBucket + "]";
        }
    }

    private static class SearchDepthParameter extends Parameter {
        private final int emptyBucket;
        private final int evalBucket;

        public SearchDepthParameter(int emptyBucket, int evalBucket) {
            super(-1);
            this.emptyBucket = emptyBucket;
            this.evalBucket = evalBucket;
        }

        @Override int get() {
            return MoveSorter.searchDepths[emptyBucket][evalBucket];
        }

        @Override void set(int value) {
            MoveSorter.searchDepths[emptyBucket][evalBucket] = value;
        }

        @Override public String toString() {
            return "searchDepths[" + emptyBucket + "][" + evalBucket + "]";
        }
    }
}

/**
 * Something whose performance can be estimated, either by timing its run() method or by counting its nodes.
 */
interface Tunable {
    /**
     * Run the method and return its cost.
     * <p/>
     * The Tuner may use either the return value of this method, or the elapsed time taken by this method, to tune parameters.
     *
     * @return cost of the method
     */
    double cost();
}
