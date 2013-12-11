package com.welty.novello.eval;

/**
 * A CoefficientSet holds coefficients for an evaluation strategy.
 * <p/>
 * slices[nEmpty][iFeature][instance] holds the value for a given instance.
 * <p/>
 * slices[nEmpty] is called a "slice"
 */
class CoefficientSet {
    private final short[][][] slices;
    final String name;

    public CoefficientSet(EvalStrategy strategy, String name) {
        this(readSlices(strategy, name), name);
    }

    private CoefficientSet(short[][][] slices, String name) {
        this.slices = slices;
        this.name = name;
    }

    private static short[][][] readSlices(EvalStrategy strategy, String name) {
        short[][][] slices = new short[64][][];
        for (int nEmpty = 4; nEmpty < 64; nEmpty += 8) {
            slices[nEmpty] = strategy.readSlice(nEmpty, name);
            slices[nEmpty - 1] = strategy.readSlice(nEmpty - 1, name);
            for (int diff = -4; diff < 4; diff += 2) {
                slices[nEmpty + diff] = slices[nEmpty];
                slices[nEmpty + diff + 1] = slices[nEmpty - 1];
            }
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
