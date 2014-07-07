/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

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
