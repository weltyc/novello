package com.welty.novello.eval;

import junit.framework.TestCase;

/**
 */
public class CornerTermTest extends TestCase {
    public void testInstance() {
        final Term term = new CornerTerm(000);
        assertEquals(0, term.instance(0, 0, 0, 0));
        assertEquals(1, term.instance(0xFC, 0x2, 0x1, 0));
        assertEquals(2, term.instance(0x2, 0xFC, 0, 0x1));
        assertEquals(3, term.instance(0x01FC, 0x0101010101010002L, 0x1, 0x1));
        assertEquals(4, term.instance(0x1, 0, 0, 0));
        assertEquals(5, term.instance(0, 0x1, 0, 0));
    }
}
