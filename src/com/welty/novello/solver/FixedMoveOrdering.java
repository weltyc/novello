package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;

/**
 * fixed move ordering
 * <p/>
 * This is used at low heights and also when breaking ties at higher heights.
 */
class FixedMoveOrdering {
    private static final int[] squareValueTable = new int[64];

    /**
     * This is used to order the moves at high depths (when we're sorting).
     *
     * @param sq square index
     * @return value of moving to the square. Higher is better.
     */
    public static int getValue(int sq) {
        return squareValueTable[sq];
    }

    private static final long type_B2 = 0x0042000000004200L;
    private static final long type_A2 = 0x4281000000008142L;
    private static final long type_A3 = 0x2400810000810024L;
    private static final long type_B3 = 0x0024420000422400L;

    /**
     * This ordering is used at low depths (below the sort depth).
     *
     * At high depths, moves are scored using getValue();
     */
    static final long[] masks = {
            BitBoardUtils.CORNERS,
            ~(BitBoardUtils.CORNERS | type_B2 | type_A2 | type_A3 | type_B3),
            type_B3,
            type_A3,
            type_A2,
            type_B2
    };

    private static final int[] values = {
            24,
            17,
            15,
            13,
            11,
            5
    };

    static {
        // internal check of masks
        long combined = 0;
        for (long mask : masks) {
            if ((combined & mask) != 0) {
                throw new IllegalStateException("mask check failed");
            }
            combined |= mask;
        }
        if (combined != -1) {
            throw new IllegalStateException("Mask set doesn't cover all squares");
        }

        // initialize square value table
        for (int j = 0; j < masks.length; j++) {
            final long mask = masks[j];
            setValue(mask, values[j]);
        }
    }

    static void setValue(long mask, int value) {
        while (mask != 0) {
            final int sq = Long.numberOfTrailingZeros(mask);
            squareValueTable[sq] = value;
            mask ^= 1L << sq;
        }
    }
}
