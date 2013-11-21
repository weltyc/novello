package com.welty.novello.eval;

/**
 * A CoefficientSet holds coefficients for an evaluation strategy.
 *
 * slices[nEmpty][iFeature][instance] holds the value for a given instance.
 *
 * slices[nEmpty] is called a "slice"
*/
class CoefficientSet {
    private final int[][][] slices = new int[64][][];

    public CoefficientSet(EvalStrategy strategy, String coeffSetName) {
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            slices[nEmpty] = strategy.readSlice(nEmpty, coeffSetName);
        }
    }

    /**
     * Get a coefficient slice containing the coefficients at a given # empties
     *
     * @param nEmpty number of empty squares
     * @return slice[iFeature][instance] is the eval coefficient
     */
    public int[][] slice(int nEmpty) {
        return slices[nEmpty];
    }
}
