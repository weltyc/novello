package com.welty.novello.eval;

/**
 * Lookup table for orids
 */
public class OridTable {
    public static int orid10(int instance) {
        return orid10[instance];
    }

    public static int orid8(int instance8) {
        return orid8[instance8];
    }

    public static int orid7(int instance) {
        return orid7[instance];
    }

    public static int orid6(int instance) {
        return orid6[instance];
    }

    public static int orid5(int instance) {
        return orid5[instance];
    }

    public static int orid4(int instance) {
        return orid4[instance];
    }

    private static int[] orid10 = calcOrids(10);
    private static int[] orid8 = calcOrids(8);
    private static int[] orid7 = calcOrids(7);
    private static int[] orid6 = calcOrids(6);
    private static int[] orid5 = calcOrids(5);
    private static int[] orid4 = calcOrids(4);

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
