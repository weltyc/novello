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
public class RowTermTest extends TestCase {
    public void testGetInstance() {
        final RowTerm bottom = new RowTerm(0);
        final int expected = Base3.base2ToBase3(0x31, 0x82);
        assertEquals(expected, bottom.instance(0x31, 0x82, 0, 0));
        assertEquals(expected, bottom.instance(0xF0FFFFFFFFFFFF31L, 0x0FFFFFFFFFFFFF82L, 0, 0));


        final RowTerm top = new RowTerm(7);
        assertEquals(expected, top.instance(0x3100000000000000L, 0x8200000000000000L, 0, 0));
        assertEquals(expected, top.instance(0x31FFFFFFFFFFFFF0L, 0x82FFFFFFFFFFFF0FL, 0, 0));

    }
}
