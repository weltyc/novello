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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Calculates statistics after throwing out top and bottom 1/4 of the data.
 * <p/>
 * Helps to reduce impact of timing outliers
 */
public class Typical  {
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

    public double q1() {
        return q1;
    }

    public double q3() {
        return q3;
    }

    /**
     * Time how long the runnable takes, and return typical timings.
     *
     * @param tunable runnable to be executed
     * @param nIters number of times to execute the runnable
     * @return typical timing for the runs.
     */
    public static @NotNull Typical timing(@NotNull Tunable tunable, int nIters) {
        final double[] timings = new double[nIters];

        for (int i = 0; i < nIters; i++) {
            final long t0 = System.currentTimeMillis();
            tunable.cost();
            final long dt = System.currentTimeMillis() - t0;
            timings[i] = dt;
        }

        return new Typical(timings);
    }

    /**
     * Time how long the runnable takes, and return typical timings.
     *
     * Calls {@link #timing(Runnable, int)} with 16 iterations.
     *
     * @param tunable Tunable to be executed
     * @return typical timing for the runs.
     */
    public static @NotNull Typical timing(@NotNull Tunable tunable) {
        return timing(tunable, 16);
    }
}
