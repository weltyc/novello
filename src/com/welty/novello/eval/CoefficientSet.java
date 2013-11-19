package com.welty.novello.eval;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
*/
class CoefficientSet {
    private final int[][][] coeffs = new int[64][][];

    public CoefficientSet(EvalStrategy strategy) {
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            final String filename = strategy.getFilename(nEmpty);
            try (DataInputStream in = new DataInputStream(new FileInputStream(filename))) {
                final int nFeatures = strategy.nFeatures();
                coeffs[nEmpty] = new int[nFeatures][];

                for (int iFeature = 0; iFeature<nFeatures; iFeature++) {
                    final Feature feature = strategy.getFeature(iFeature);

                    final int[] coeffsByOrid = readInts(in, feature.nOrids());
                    coeffs[nEmpty][iFeature] = coeffsByInstance(coeffsByOrid, feature);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Convert coeffsByOrid (as read from a file) to coeffsByInstance (as used in the eval)
     *
     * @param coeffsByOrid array containing coefficients for each orid
     * @param feature feature to map instances to orids
     * @return array containing coefficients for each instance.
     */
    private static int[] coeffsByInstance(int[] coeffsByOrid, Feature feature) {
        final int n = feature.nInstances();
        final int[] coeffsByInstance = new int[n];
        for (int i = 0; i < n; i++) {
            coeffsByInstance[i] = coeffsByOrid[feature.orid(i)];
        }
        return coeffsByInstance;
    }

    private static int[] readInts(DataInputStream in, int nOrids) throws IOException {
        final int[] coeffsByOrid = new int[nOrids];
        for (int i=0; i<nOrids; i++) {
            coeffsByOrid[i]=in.readInt();
        }
        return coeffsByOrid;
    }

    /**
     * Get a coefficient slice containing the coefficients at a given # empties
     *
     * @param nEmpty number of empty squares
     * @return slice[iFeature][instance] is the eval coefficient
     */
    public int[][] slice(int nEmpty) {
        return coeffs[nEmpty];
    }
}
