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

import com.welty.novello.solver.BitBoardTestCase;

/**
 */
public class UrdlTermTest extends BitBoardTestCase {
    private static final UrdlTerm term0 = new UrdlTerm(0);
    private static final UrdlTerm term2 = new UrdlTerm(2);
    private static final UrdlTerm termM3 = new UrdlTerm(-3);

    public void testMask() {
        assertEquals(0x0102040810204080L, UrdlTerm.urdlMask(0));
        assertEquals(0x010204081020L, UrdlTerm.urdlMask(2));
        assertBitBoardEquals(0x0408102040800000L, UrdlTerm.urdlMask(-2));
    }

    public void testExtractDiagonal() {
        assertEquals(255, term0.extractDiagonal(-1));
        assertEquals(1, term0.extractDiagonal(1L<<56));

        assertEquals(63, term2.extractDiagonal(-1));
        assertEquals(1, term2.extractDiagonal(1L<<40));

        assertEquals(31, termM3.extractDiagonal(-1));
        assertEquals(1, termM3.extractDiagonal(1L<<59));
    }

    public void testInstance() {
        assertEquals(6560, term0.instance(0, -1L, 0, 0));
        assertEquals(3280, term0.instance(-1L, 0, 0, 0));
        assertEquals(2+3+18+27, term0.instance(0x0002000800000000L, 0x0100040000000000L, 0, 0));

        assertEquals(728, term2.instance(0, -1L, 0, 0));
        assertEquals(2+3+18+27, term2.instance(0x000200080000L, 0x010004000000L, 0, 0));

        assertEquals(242, termM3.instance(0, -1L, 0, 0));
        assertEquals(2+3+18+27, termM3.instance(0x0010004000000000L, 0x0800200000000000L, 0, 0));

    }
}
