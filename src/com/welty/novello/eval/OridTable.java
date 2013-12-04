package com.welty.novello.eval;

/**
 * Lookup table for orids
 */
public class OridTable {
    public static int orid8(int instance8) {
        return orid8[instance8];
    }

    private static int[] orid8 = calcOrids(8);

    private static int[] calcOrids(int nDisks) {
        final int[] orids = new int[Base3.nInstances(nDisks)];

        int nOrids = 0;
        for (int instance = 0; instance < orids.length; instance++) {
            final int reverse = Base3.reverse(instance, nDisks);
            if (reverse < instance) {
                orids[instance] = orids[reverse];
            } else {
                orids[instance] = nOrids++;
            }
        }
        return orids;
    }
}
