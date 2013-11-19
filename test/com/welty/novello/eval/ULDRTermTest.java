package com.welty.novello.eval;

import junit.framework.TestCase;

/**
 */
public class ULDRTermTest extends TestCase {
    public void testBase2ToBase3() {
        testBase2ToBase3(0, 0, 0);
        testBase2ToBase3(1, 0, 1);
        testBase2ToBase3(0, 1, 2);
        testBase2ToBase3(2, 0, 3);
        testBase2ToBase3(3, 0, 4);
        testBase2ToBase3(2, 1, 5);
        testBase2ToBase3(0, 2, 6);
    }

    public void testInstance() {
        final ULDRTerm term = new ULDRTerm();
        assertEquals(0, term.instance(0,0,0,0));
        assertEquals(0, term.instance(0x7F3F1F0F07030100L, 0x0080C0E0F0F8FCFEL, 0, 0));
        assertEquals(1, term.instance(1,0,0,0));
        assertEquals(2, term.instance(0,1,0,0));
        assertEquals(3, term.instance(0x0200, 0,0,0));
        assertEquals(4, term.instance(0x0201, 0, 0, 0));
        assertEquals(6, term.instance(0, 0x0200, 0, 0));
    }

    public void testShift() {
        assertEquals(0, ULDRTerm.shift(0));
        assertEquals(0, ULDRTerm.shift(0xFE));
        assertEquals(1, ULDRTerm.shift(1));
        assertEquals(2, ULDRTerm.shift(0x0200));
    }

    private void testBase2ToBase3(int mover, int enemy, int expected) {
        assertEquals(expected, Base3.base2ToBase3(mover, enemy));
    }
}
