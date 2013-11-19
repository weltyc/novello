package com.welty.novello.solver;

/**
 */
public class BitBoardTest extends BitBoardTestCase {
    public void testMinimalReflection() {
        final long black = 0x3141592653589793L;
        final long white = 0x2718281828459045L &~black;

        final BitBoard bb = new BitBoard(black, white, true);
        final BitBoard minimal = bb.minimalReflection();
        for (int r=0; r<8; r++) {
            final BitBoard reflection = bb.reflection(r);
            assertTrue(minimal.compareTo(reflection)<0 || minimal.equals(reflection));
        }
    }
}
