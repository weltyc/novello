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

class OridHistogram {
    private static final int MAX_LOG = 4;

    private final int[] counts;

    OridHistogram(PositionElement[] elements, int nIndices) {
        counts = new int[nIndices];
        for (PositionElement element : elements) {
            element.updateHistogram(counts);
        }

    }

    public void dump() {
        final int[] counts = this.counts;
        final int[] histogram = createLogHistogram(counts);
        for (int i = 0; i < histogram.length; i++) {
            final int h = histogram[i];
            if (h > 0) {
                System.out.format("%,8d coefficients occurred %s%n", h, rangeText(i));
            }
        }
        System.out.println();
    }

    static int[] createLogHistogram(int[] counts) {
        final int[] histogram = new int[MAX_LOG+1];
        for (int count : counts) {
            final int log10;
            if (count==0) {
                log10 = 0;
            }
            else {
                log10 = Math.min(MAX_LOG, 1 + (int)Math.log10(count));
            }
            histogram[log10]++;
        }
        return histogram;
    }


    private static String rangeText(int i) {
        if (i == 0) {
            return "0 times";
        }
        final int min = (int)Math.pow(10, i-1);
        if (i == MAX_LOG) {
            return min + "+ times";
        }
        final int max = (int)Math.pow(10, i)-1;
        return min + "-" + max + " times";
    }
}
