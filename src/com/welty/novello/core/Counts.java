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

    private static char[] prefixes = " kMGTPE".toCharArray();

    @Override public String toString() {
        int prefix = 1;
        int prefixIndex = 0;
        for (long i=nFlips; i/prefix>=100000; ) {
            prefix*=1000;
            prefixIndex++;
        }
        return toString(prefixIndex);
    }

    /**
     *
     * @param prefixIndex display prefix for counts: 0=units, 1=k, 2=M, 3=T etc.
     * @return string representation of the counts
     */
    public @NotNull String toString(int prefixIndex) {
        return format(nFlips, prefixIndex) + "n  " + format(nEvals,prefixIndex) + "evals ";
    }

    private String format(long nFlips, int prefixIndex) {
        for (int i=0; i<prefixIndex; i++) {
            nFlips /=1000;
        }
        return String.format("%,5d %c", nFlips, prefixes[prefixIndex]);
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
