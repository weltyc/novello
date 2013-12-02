package com.welty.novello.eval;

import junit.framework.TestCase;

/**
 */
public class LinePatternFeatureFactoryTest extends TestCase {

    public void test8() throws Exception {
        final Feature feature = LinePatternFeatureFactory.of("Foo", 8);

        assertEquals(6561, feature.nInstances());
        assertEquals(3321, feature.nOrids());
        assertEquals(3320, feature.orid(6560));
        assertEquals(feature.orid(3 * 729), feature.orid(1));
    }

    public void test3() throws Exception {
        final Feature feature = LinePatternFeatureFactory.of("Foo", 3);

        assertEquals(27, feature.nInstances());
        assertEquals(18, feature.nOrids());
        assertEquals(17, feature.orid(26));
        assertEquals(feature.orid(9), feature.orid(1));
    }
}
