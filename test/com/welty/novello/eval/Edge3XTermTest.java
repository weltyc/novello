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

import static com.welty.novello.eval.Edge3XTerm.*;

public class Edge3XTermTest extends TestCase {
    public void testExtractBottom() {
        assertEquals(0, extractBottom(0));
        assertEquals(1, extractBottom(1));
        assertEquals(0x100, extractBottom(0x200));
    }

    public void testExtract3X() {
        assertEquals(0, extract3X(0, 0));
        assertEquals(1, extract3X(1, 0));
        assertEquals(0, extract3X(0, 1));
        assertEquals(0x100, extract3X(0, 2));

        // center virtual disk
        for (int i=0; i<15; i++) {
            assertEquals(0, extract3X(0, i << 2));
        }
        assertEquals(0x200, extract3X(0, 15 << 2));

        assertEquals(0x400, extract3X(0, 0x40));
    }

    public void testCalcSecondRowValue() {
        assertEquals(0, calcSecondRowValue(0));
        assertEquals(0, calcSecondRowValue(1));
        assertEquals(0x100, calcSecondRowValue(2));
        assertEquals(0x100, calcSecondRowValue(3));
        assertEquals(0, calcSecondRowValue(4));
        assertEquals(0x200, calcSecondRowValue(0b00111100));
        assertEquals(0x400, calcSecondRowValue(0x40));
        assertEquals(0, calcSecondRowValue(0x80));
    }

    public void testInstance0() {
        assertEquals(0, instance0(0, 0));
        final int all2s = 27 * 6561 - 1;
        assertEquals(all2s/2, instance0(-1, 0));
        assertEquals(all2s, instance0(0, -1));
    }
}
