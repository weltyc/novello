package com.welty.novello.eval;

/**
 * A CoefficientSet holds coefficients for an evaluation strategy.
 *
 * slices[nEmpty][iFeature][instance] holds the value for a given instance.
 *
 * slices[nEmpty] is called a "slice"
*/
class CoefficientSet {
    private final short[][][] slices;
    private final String name;

    public CoefficientSet(EvalStrategy strategy, String name) {
        this (readSlices(strategy, name), name);
    }

    private CoefficientSet(short[][][] slices, String name) {
        this.slices = slices;
        this.name = name;
    }

    private static short[][][] readSlices(EvalStrategy strategy, String name) {
        short[][][] slices = new short[64][][];
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            slices[nEmpty] = strategy.readSlice(nEmpty, name);
        }
        return slices;
    }

    /**
     * Get a coefficient slice containing the coefficients at a given # empties
     *
     * @param nEmpty number of empty squares
     * @return slice[iFeature][instance] is the eval coefficient
     */
    public short[][] slice(int nEmpty) {
        return slices[nEmpty];
    }

    @Override public String toString() {
        return name;
    }
}
