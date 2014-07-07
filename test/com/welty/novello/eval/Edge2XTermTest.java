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
public class Edge2XTermTest extends TestCase {
    public void testGetInstance() {
        final Edge2XTerm bottom = new Edge2XTerm(0);
        final int expected = Base3.base2ToBase3(2*0x31 + 1, 0x200 + 2*0x82);
        assertEquals(expected, bottom.instance(0x0231, 0x4082, 0, 0));
        assertEquals(expected, bottom.instance(0xF0FFFFFFFFFF0F31L, 0x0FFFFFFFFFFFF082L, 0, 0));


        final Edge2XTerm top = new Edge2XTerm(1);
        assertEquals(expected, top.instance(0x3102000000000000L, 0x8240000000000000L, 0, 0));
        assertEquals(expected, top.instance(0x310FFFFFFFFFFFF0L, 0x82F0FFFFFFFFFF0FL, 0, 0));

        final Edge2XTerm left = new Edge2XTerm(2);
        assertEquals(expected, left.instance(0x8040000080800000L, 0x0080000000004080L, 0, 0));

        final Edge2XTerm right = new Edge2XTerm(3);
        assertEquals(expected, right.instance(0x0102000001010000L, 0x0001000000000201L, 0, 0));
    }
}
