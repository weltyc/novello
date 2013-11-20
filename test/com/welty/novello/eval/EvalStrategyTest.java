package com.welty.novello.eval;

import com.orbanova.common.misc.ArrayTestCase;
import com.orbanova.common.misc.Vec;
import com.orbanova.common.ramfs.RamFileSystem;

import java.io.IOException;
import java.nio.file.Path;

/**
 */
public class EvalStrategyTest extends ArrayTestCase {
    public void testIndicesFromPosition() {
        final EvalStrategy strategy = EvalStrategies.eval1;

        // All 4 terms share the same feature
        assertEquals(1, strategy.nFeatures());

        // a sample position.
        // square 077 is mover, square 070 is enemy,
        // square 007 has mover access, square 000 has enemy access
        final long mover = 0x8001010101010100L;
        final long enemy = 0x0180808080808000L;
        final int[] expected = {2, 1, 5, 4};
        assertEquals(expected, strategy.coefficientIndices(mover, enemy));
    }

    public void testFeatureCompression() {

        final EvalStrategy strategy = new EvalStrategy("test",
                TermTest.term1,
                TermTest.term1,
                TermTest.term2
        );

        assertEquals(2, strategy.nFeatures());
        assertEquals(new int[]{3, 2}, strategy.nOridsByFeature());
        assertEquals(TermTest.feature1.nOrids() + TermTest.feature2.nOrids(), strategy.nCoefficientIndices());
        assertEquals(new int[] {2, 2, 3}, strategy.coefficientIndices(2, 0));
    }

    public void testWriteRead() throws IOException {
        // specific strategy doesn't matter too much for this test, just want it to have
        // multiple terms and features.
        final EvalStrategy strategy = EvalStrategies.diagonal;
        final int nFeatures = strategy.nFeatures();

        final double[] coeffs = Vec.increasingDouble(0., .01, strategy.nCoefficientIndices());
        final RamFileSystem fs = new RamFileSystem();
        final Path coefficientDirectory = fs.getPath("coefficients");
        final int nEmpty = 12;
        strategy.writeSlice(nEmpty, coeffs, coefficientDirectory);

        final int[][] slice = strategy.readCompressedSlice(nEmpty, coefficientDirectory);
        assertEquals(nFeatures, slice.length);

        // test expected result for each feature
        int value=0;
        for (int iFeature = 0; iFeature < nFeatures; iFeature++) {
            final Feature feature = strategy.getFeature(iFeature);
            final int nOrids=  feature.nOrids();
            int[] expected= Vec.increasingInt(value, 1, nOrids);
            value += nOrids;

            assertEquals(expected, slice[iFeature]);
        }
    }

    public void testDecompress() {
        final EvalStrategy strategy = EvalStrategies.diagonal;
        final int nFeatures = strategy.nFeatures();

        // compressed data. coefficient = orid
        final int[][] slice = new int[nFeatures][];
        for (int iFeature=0; iFeature<nFeatures; iFeature++) {
            final Feature feature = strategy.getFeature(iFeature);
            slice[iFeature] = Vec.increasingInt(0, 1, feature.nOrids());
        }

        // decompress slice. This happens in place, so no return value.
        strategy.decompressSlice(slice);
        for (int iFeature=0; iFeature<nFeatures; iFeature++) {
            final Feature feature = strategy.getFeature(iFeature);
            final int nInstances = feature.nInstances();
            assertEquals(nInstances, slice[iFeature].length);
            for (int i=0; i< nInstances; i++) {
                assertEquals(feature.orid(i), slice[iFeature][i]);
            }
        }

    }

}
