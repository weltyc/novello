package com.welty.novello.eval;

import junit.framework.TestCase;

import static org.junit.Assert.assertArrayEquals;

public class OridHistogramTest extends TestCase {
    public void testCreateLogHistogram() throws Exception {
        final int[] counts = {0, 1, 10, 100, 1000};
        assertArrayEquals("", new int[]{1, 1, 1, 1, 1}, OridHistogram.createLogHistogram(counts));

        final int[] counts2= {0, 0, 1, 9, 10, 99, 100, 999, 1000, 9999, 10000, 99999};
        assertArrayEquals("", new int[]{2, 2, 2, 2, 4}, OridHistogram.createLogHistogram(counts2));


    }
}
