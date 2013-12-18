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

    public void testEngineering() {
        testEngineeringLong("     0  ", (long) 0);
        testEngineeringLong("     3  ", (long) 3);
        testEngineeringLong("    12  ", (long) 12);
        testEngineeringLong("   123  ", (long) 123);
        testEngineeringLong(" 1,234  ", (long) 1234);
        testEngineeringLong("12,345  ", (long) 12345);
        testEngineeringLong("99,999  ", (long) 99999);
        testEngineeringLong("   100 k", (long) 100000);
        testEngineeringLong("   123 k", (long) 123456);
        testEngineeringLong(" 1,234 k", (long) 1234568);
        testEngineeringLong("12,345 k", (long) 12345678);
        testEngineeringLong("   123 M", (long) 123456789);
    }


    private static void testEngineeringLong(String expected, long input) {
        assertEquals(expected, NovelloUtils.engineeringLong(input));
    }

    public void testEngineeringDouble() {
        testEngineeringDouble("  0.00  ", 0);
        testEngineeringDouble("  3.00  ",  3);
        testEngineeringDouble("  12.0  ",  12);
        testEngineeringDouble(" 123.00  ",  123);
        testEngineeringDouble("   1.23 k",  1234);
        testEngineeringDouble("  12.34 k",  12344);
        testEngineeringDouble("  99.99 k",  99990);
        testEngineeringDouble(" 100.00 k",  100000);
        testEngineeringDouble(" 123.45 k",  123450);
        testEngineeringDouble(" 999.99 k",  999994);
        testEngineeringDouble("   1.00 M",  999995);
        testEngineeringDouble("   1.23 M",  1230000);
        testEngineeringDouble("  12.34 M",  12340000);
        testEngineeringDouble(" 123.45 M",  123450000);

        testEngineeringDouble(" 999.99 m", 0.99999);
        testEngineeringDouble(" 100.00 m", 0.1);
        testEngineeringDouble(" 100.00 m", 0.099995);
        testEngineeringDouble("  99.99 m", 0.099994);
        testEngineeringDouble("  10.00 m", 0.01);
        testEngineeringDouble("  10.00 m", 0.009995);
        testEngineeringDouble("   9.99 m", 0.009994);
        testEngineeringDouble("   1.00 m", 0.001);
        testEngineeringDouble("   1.00 m", 0.000999995);
        testEngineeringDouble(" 999.99 μ", 0.000999994);
        testEngineeringDouble("   1.00 μ", 0.000001);

        testEngineeringDouble("-999.99 m", -0.99999);

    }


    private static void testEngineeringDouble(String expected, double input) {
        assertEquals(expected, NovelloUtils.engineeringDouble(input));
    }

}
