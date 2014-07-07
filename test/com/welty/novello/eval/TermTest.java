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
public class TermTest extends TestCase {
    static final Feature feature1 = new SoloFeature("test", "orid 1", "orid 2", "orid 3");
    static final Feature feature2 = new MultiFeature("test", new int[]{0,1,0}, new String[]{"orid 0", "orid 1"});

    /**
     * A sample term whose instance is (# mover disks)%3 and whose orid is the same
     */
    static final Term term1 = new Term(feature1) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return Long.bitCount(mover)%3;
        }
    };

    /**
     * A sample term whose instance is (# mover disks)%3 and whose orid is instance==1?1:0
     */
    static final Term term2 = new Term(feature2) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return Long.bitCount(mover)%3;
        }
    };

    public void testOrid() {
        for (long mover = 0; mover < feature1.nInstances(); mover++) {
            final int instance = term1.instance(mover, 0, 0, 0);
            final int expected = feature1.orid(instance);
            assertEquals(expected, term1.orid(mover, 0, 0, 0));
        }
        for (long mover = 0; mover < feature2.nInstances(); mover++) {
            final int instance = term2.instance(mover, 0, 0, 0);
            final int expected = feature2.orid(instance);
            assertEquals(expected, term2.orid(mover, 0, 0, 0));
        }
    }

    public void testGetFeature() {
        assertEquals(feature1, term1.getFeature());
        assertEquals(feature2, term2.getFeature());
    }
}
