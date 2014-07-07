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

package com.welty.novello.core;

import com.orbanova.common.misc.Engineering;
import org.jetbrains.annotations.NotNull;

/**
 */
public class Counts {
    public final long nFlips;
    public final long nEvals;

    public Counts(long nFlips, long nEvals) {
        this.nFlips = nFlips;
        this.nEvals = nEvals;
    }

    @Override public String toString() {
        final long x = nFlips;
        int prefixIndex = Engineering.calcPrefix(x);
        return toString(prefixIndex);
    }

    /**
     *
     * @param prefixIndex display prefix for counts: 0=units, 1=k, 2=M, 3=T etc.
     * @return string representation of the counts
     */
    public @NotNull String toString(int prefixIndex) {
        return Engineering.formatLong(nFlips, prefixIndex) + "n, " + Engineering.formatLong(nEvals, prefixIndex) + "evals, " + Engineering.formatLong(cost(), prefixIndex) + "$";
    }

    public Counts plus(Counts counts) {
        return new Counts(nFlips + counts.nFlips, nEvals + counts.nEvals);
    }

    /**
     * Estimate the cost of the search
     * @return a number proportional to the cost of the search
     */
    public long cost() {
        return nFlips + 5*nEvals;
    }

    public Counts minus(Counts counts) {
        return new Counts(nFlips - counts.nFlips, nEvals - counts.nEvals);
    }
}
