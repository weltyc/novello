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

package com.welty.novello.solver;

import com.welty.novello.core.Board;
import junit.framework.TestCase;

/**
 */
public abstract class BitBoardTestCase extends TestCase {
    public static void assertBitBoardEquals(long expected, long actual) {
        assertBitBoardEquals(null, expected, actual);
    }

    public static void assertBitBoardEquals(String msg, long expected, long actual) {
        if (expected!=actual) {
            String expectedText = new Board(expected, 0, false).toString();
            String actualText = new Board(actual, 0, false).toString();
            assertEquals(msg, expectedText, actualText);
        }
    }
}
