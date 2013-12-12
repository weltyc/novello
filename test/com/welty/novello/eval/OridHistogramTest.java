package com.welty.novello.eval;

import com.orbanova.common.misc.ArrayTestCase;

public class OridHistogramTest extends ArrayTestCase {
    public void testCreateLogHistogram() throws Exception {
        final int[] counts = {0, 1, 10, 100, 1000};
        assertEquals(new int[]{1, 1, 1, 1, 1}, OridHistogram.createLogHistogram(counts));

        final int[] counts2= {0, 0, 1, 9, 10, 99, 100, 999, 1000, 9999, 10000, 99999};
        assertEquals(new int[]{2, 2, 2, 2, 4}, OridHistogram.createLogHistogram(counts2));


    }
}
