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
public class Uldr_TermTest extends BitBoardTestCase {
    private static final UldrTerm term0 = new UldrTerm(0);
    private static final UldrTerm term2 = new UldrTerm(2);
    private static final UldrTerm termM3 = new UldrTerm(-3);

    public void testMask() {
        assertBitBoardEquals(0x8040201008040201L, UldrTerm.uldrMask(0));
        assertBitBoardEquals(0x2010080402010000L, UldrTerm.uldrMask(2));
        assertBitBoardEquals(0x804020100804L, UldrTerm.uldrMask(-2));
    }

    public void testExtractDiagonal() {
        assertEquals(255, term0.extractDiagonal(-1));
        assertEquals(128, term0.extractDiagonal(1L<<63));

        assertEquals(63, term2.extractDiagonal(-1));
        assertEquals(32, term2.extractDiagonal(1L<<61));

        assertEquals(31, termM3.extractDiagonal(-1));
        assertEquals(16, termM3.extractDiagonal(1L<<39));
    }

    public void testInstance() {
        assertEquals(6560, term0.instance(0, -1L, 0, 0));
        assertEquals(3280, term0.instance(-1L, 0, 0, 0));
        assertEquals(2+3+18+27, term0.instance(0x08000200L, 0x040001L, 0, 0));

        assertEquals(728, term2.instance(0, -1L, 0, 0));
        assertEquals(2+3+18+27, term2.instance(0x080002000000L, 0x0400010000L, 0, 0));

        assertEquals(242, termM3.instance(0, -1L, 0, 0));
        assertEquals(2+3+18+27, termM3.instance(0x40001000L, 0x200008L, 0, 0));

        final UldrTerm term = new UldrTerm(0);
        assertEquals(0, term.instance(0,0,0,0));
        assertEquals(0, term.instance(0x7F3F1F0F07030100L, 0x0080C0E0F0F8FCFEL, 0, 0));
        assertEquals(1, term.instance(1,0,0,0));
        assertEquals(2, term.instance(0,1,0,0));
        assertEquals(3, term.instance(0x0200, 0,0,0));
        assertEquals(4, term.instance(0x0201, 0, 0, 0));
        assertEquals(6, term.instance(0, 0x0200, 0, 0));
    }
}
