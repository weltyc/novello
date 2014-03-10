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
