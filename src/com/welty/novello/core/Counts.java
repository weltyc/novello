package com.welty.novello.core;

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
        int prefixIndex = NovelloUtils.calcPrefix(x);
        return toString(prefixIndex);
    }

    /**
     *
     * @param prefixIndex display prefix for counts: 0=units, 1=k, 2=M, 3=T etc.
     * @return string representation of the counts
     */
    public @NotNull String toString(int prefixIndex) {
        return NovelloUtils.format(nFlips, prefixIndex) + "n, " + NovelloUtils.format(nEvals, prefixIndex) + "evals, " + NovelloUtils.format(cost(), prefixIndex) + "$";
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
}
