package com.welty.novello.eval;

import junit.framework.TestCase;

/**
 */
public class Edge3XFeatureTest extends TestCase {
    private static final Edge3XFeature feature = Edge3XFeature.instance;

    public void testNOrids() throws Exception {
        assertEquals(88938, feature.nOrids());
    }

    public void testOridDescription() throws Exception {
        assertEquals(".../........", feature.oridDescription(0));
        assertEquals(".../.......*", feature.oridDescription(1));
    }

    public void testNInstances() throws Exception {
        assertEquals(3 * 9 * 6561, feature.nInstances());
    }

    public void testOrid() throws Exception {
        assertEquals(0, feature.orid(0));
//        assertEquals(29888, feature.orid(9*6561-1));
        assertEquals(1, feature.orid(1));
        assertEquals(feature.orid(9 + 3 + 1), feature.orid(2187 + 729 + 243));
        assertEquals(feature.orid(2 * 9 * 6561), feature.orid(2 * 6561));
    }
}
