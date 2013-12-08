package com.welty.novello.solver;

import junit.framework.TestCase;

import java.util.Random;

/**
 */
public class MurmurTest extends TestCase {
    // counts[byte index 0-7][byte] = number of times a collision has appeared.
    private long nHashes = 0;
    private final int[][] counts = new int[8][256];
    private final Random r = new Random(1337);
    private final long empty = r.nextLong() & r.nextLong();
    private final long mover = ~empty & r.nextLong();
    private final long enemy = ~(mover | empty);

    private void insert(long mover, long enemy) {
        final long hash = Murmur.hash(mover, enemy);
        for (int bi = 0; bi < 8; bi++) {
            counts[bi][(int) ((hash >> bi * 8) & 0xFF)]++;
        }
        nHashes++;
    }

    public void testSingleBitChanges() {
        insertChanges(3, 64, mover, enemy);
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
