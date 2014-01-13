package com.welty.novello.eval;

import junit.framework.TestCase;

/**
 */
public class CornerTriangleFeatureTest extends TestCase {
    private static final CornerTriangleFeature feature = CornerTriangleFeature.instance;

    public void testNOrids() throws Exception {
        assertEquals(29889, feature.nOrids());
    }

    public void testOridDescription() throws Exception {
        assertEquals("./../.../.... <-- corner", feature.oridDescription(0));
        assertEquals("./../.../...* <-- corner", feature.oridDescription(1));
    }

    public void testNInstances() throws Exception {
        assertEquals(9*6561, feature.nInstances());
    }

    public void testOrid() throws Exception {
        assertEquals(0, feature.orid(0));
        assertEquals(29888, feature.orid(9*6561-1));
        assertEquals(1, feature.orid(1));
        assertEquals(feature.orid(9+3+1), feature.orid(2187+81+1));
        assertEquals(feature.orid(2*6561 + 2187), feature.orid(2*729+ 9));
    }
}
