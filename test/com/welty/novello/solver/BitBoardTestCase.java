package com.welty.novello.solver;

import junit.framework.TestCase;

/**
 */
public abstract class BitBoardTestCase extends TestCase {
    static void assertBitBoardEquals(long expected, long actual) {
        assertBitBoardEquals(null, expected, actual);
    }

    static void assertBitBoardEquals(String msg, long expected, long actual) {
        if (expected!=actual) {
            String expectedText = new BitBoard(expected, 0, false).toString();
            String actualText = new BitBoard(actual, 0, false).toString();
            assertEquals(msg, expectedText, actualText);
        }
    }
}
