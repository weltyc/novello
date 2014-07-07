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
public class MultiFeatureTest extends TestCase {

    private static final MultiFeature multiFeature = new MultiFeature("test", new int[]{0, 1, 0}, new String[]{"orid 0", "orid 1"});

    public void testOrid() {
        assertEquals(0, multiFeature.orid(0));
        assertEquals(1, multiFeature.orid(1));
        assertEquals(0, multiFeature.orid(2));
    }
    
    public void testNOrids() {
        assertEquals(2, multiFeature.nOrids());
    }
    
    public void testNInstances() {
        assertEquals(3, multiFeature.nInstances());
    }

    public void testOridDescription() {
        assertEquals("orid 0", multiFeature.oridDescription(0));
        assertEquals("orid 1", multiFeature.oridDescription(1));
    }
}
