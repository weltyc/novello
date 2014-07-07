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

package com.welty.novello.core;

import junit.framework.TestCase;

/**
 */
public class NovelloUtilsTest extends TestCase {
    // counts[byte index 0-7][byte] = number of times a collision has appeared.
    private long nHashes = 0;
    private final int[][] counts = new int[8][256];

    private void insert(long mover, long enemy) {
        final long hash = NovelloUtils.hash(mover, enemy);
        for (int bi = 0; bi < 8; bi++) {
            counts[bi][(int) ((hash >> bi * 8) & 0xFF)]++;
        }
        nHashes++;
    }

    public void testSingleBitChanges() {
        insertChanges(3, 64, Me.late.mover, Me.late.enemy);
        check();
    }

    private void check() {
        final double expectedCollisions = nHashes / 256.;
        final double sd = Math.sqrt(expectedCollisions);
        for(int i=0; i<8; i++) {
            for (int j=0; j<256; j++) {
                final int count = counts[i][j];
                final double err = (count -expectedCollisions)/sd;
                if (Math.abs(err) >= 7) {
                    fail("Error too big for byte " + i + " value = " + j
                            + "; expected " + (int)expectedCollisions
                            + " collisions but had " + count);
                }
            }
        }
    }

    private void insertChanges(int nBitsToFlip, int highestBitToFlip, long mover, long enemy) {
        if (nBitsToFlip==0) {
            insert(mover, enemy);
        }
        else {
            for (int i=0; i<highestBitToFlip; i++) {
                final long t = 1L<<i;
                insertChanges(nBitsToFlip-1, i, mover^t, enemy);
                insertChanges(nBitsToFlip-1, i, mover, enemy^t);
                insertChanges(nBitsToFlip-1, i, mover^t, enemy^t);
            }
        }
    }
}
