package com.welty.novello.core;

import com.welty.othello.core.Engineering;
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
        return Engineering.engineeringLong(nFlips, prefixIndex) + "n, " + Engineering.engineeringLong(nEvals, prefixIndex) + "evals, " + Engineering.engineeringLong(cost(), prefixIndex) + "$";
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
