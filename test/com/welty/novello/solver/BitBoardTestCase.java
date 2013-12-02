package com.welty.novello.solver;

import com.welty.novello.core.Position;
import junit.framework.TestCase;

/**
 */
public abstract class BitBoardTestCase extends TestCase {
    public static void assertBitBoardEquals(long expected, long actual) {
        assertBitBoardEquals(null, expected, actual);
    }

    public static void assertBitBoardEquals(String msg, long expected, long actual) {
        if (expected!=actual) {
            String expectedText = new Position(expected, 0, false).toString();
            String actualText = new Position(actual, 0, false).toString();
            assertEquals(msg, expectedText, actualText);
        }
    }
}
