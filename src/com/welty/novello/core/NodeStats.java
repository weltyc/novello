package com.welty.novello.core;

/**
 */
public class NodeStats {
    public final long nFlips;
    public final long nEvals;

    public NodeStats(long nFlips, long nEvals) {
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
        return format(nFlips, prefixIndex) + " flips and " + format(nEvals,prefixIndex) + " evals ";
    }

    private String format(long nFlips, int prefixIndex) {
        for (int i=0; i<prefixIndex; i++) {
            nFlips /=1000;
        }
        return String.format("%,5d%c", nFlips, prefixes[prefixIndex]);
    }

    public NodeStats plus(NodeStats nodeStats) {
        return new NodeStats(nFlips + nodeStats.nFlips, nEvals + nodeStats.nFlips);
    }
}
