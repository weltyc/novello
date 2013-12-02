package com.welty.novello.eval;

import junit.framework.TestCase;

/**
 */
public class CornerBlockFeatureTest extends TestCase {
    private static final CornerBlockFeature feature = CornerBlockFeature.instance;

    public void testNOrids() throws Exception {
        assertEquals(10206, feature.nOrids());
    }

    public void testOridDescription() throws Exception {
        assertEquals(".../.../...", feature.oridDescription(0));
        assertEquals(".../.../..*", feature.oridDescription(1));
    }

    public void testNInstances() throws Exception {
        assertEquals(3*6561, feature.nInstances());
    }

    public void testOrid() throws Exception {
        assertEquals(0, feature.orid(0));
        assertEquals(10205, feature.orid(3*6561-1));
        assertEquals(1, feature.orid(1));
        assertEquals(feature.orid(9+3+1), feature.orid(729+27+1));
        assertEquals(feature.orid(2*6561 + 2187), feature.orid(2*6561+ 243));
    }
}
