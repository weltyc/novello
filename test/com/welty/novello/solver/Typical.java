package com.welty.novello.solver;

import com.orbanova.common.misc.Vec;

import java.util.Arrays;

/**
 * Calculates statistics after throwing out top and bottom 1/4 of the data.
 * <p/>
 * Helps to reduce impact of timing outliers
 */
class Typical {
    /**
     * mean of middle 50% of the data
     */
    private final double value;
    /**
     * first quartile value; min after lowest 1/4 is tossed
     */
    final double q1;
    /**
     * third quartile value; max after highest 1/4 is tossed
     */
    final double q3;
    /**
     * Sum of the original data, including the bits that will be tossed out
     */
    final double sum;

    Typical(double[] timings) {
        sum = Vec.sum(timings);

        final int toss = timings.length / 4;
        Arrays.sort(timings);
        timings = Arrays.copyOfRange(timings, toss, timings.length - toss);
        value = Vec.mean(timings);
        q1 = timings[0];
        q3 = timings[timings.length - 1];
    }

    @Override public String toString() {
        return String.format("%4.0f [%4.0f-%4.0f]", value, q1, q3);
    }
}
