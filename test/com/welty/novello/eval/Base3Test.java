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
public class Base3Test extends TestCase {
    public void testBase2ToBase3() {
        testBase2ToBase3(0, 0, 0);
        testBase2ToBase3(1, 0, 1);
        testBase2ToBase3(0, 1, 2);
        testBase2ToBase3(2, 0, 3);
        testBase2ToBase3(3, 0, 4);
        testBase2ToBase3(2, 1, 5);
        testBase2ToBase3(0, 2, 6);
    }

    private void testBase2ToBase3(int mover, int enemy, int expected) {
        assertEquals(expected, Base3.base2ToBase3(mover, enemy));
    }
}
