package com.welty.ntestj.table;

import com.welty.ntestj.PatternUtils;

/**
 * Corner triangle pattern.
 *
 * Trits are in this location:
 *
 * 0 4 3 2
 * 7 1 5
 * 8 6
 * 9
 *
 * Yes, really.
 */
public class RowToTriangleTable {
    private static final int[] row1ToTriangle = new int[6561];
    private static final int[] row2ToTriangle = new int[6561];
    private static final int[] row3ToTriangle = new int[6561];
    private static final int[] row4ToTriangle = new int[6561];

    static {
        final int[] trits = new int[8];
        int value1, value2, value;

        // edge->triangle translator
        for (char config = 0; config < 6561; config++) {
            PatternUtils.ConfigToTrits(config, 8, trits);
            // row 4
            value1 = trits[0] * 3 * 6561;
            value2 = trits[7] * 3 * 6561;
            value = (value1 << 16) + value2;
            row4ToTriangle[config] = value;
            // row 3
            value1 = trits[0] * 6561 + trits[1] * 729;
            value2 = trits[7] * 6561 + trits[6] * 729;
            value = (value1 << 16) + value2;
            row3ToTriangle[config] = value;
            // row 2
            value1 = trits[0] * 3 * 729 + trits[1] * 3 + trits[2] * 3 * 81;
            value2 = trits[7] * 3 * 729 + trits[6] * 3 + trits[5] * 3 * 81;
            value = (value1 << 16) + value2;
            row2ToTriangle[config] = value;
            // row 1
            value1 = trits[0] + trits[1] * 81 + trits[2] * 27 + trits[3] * 9;
            value2 = trits[7] + trits[6] * 81 + trits[5] * 27 + trits[4] * 9;
            value = (value1 << 16) + value2;
            row1ToTriangle[config] = value;
        }
    }

    /**
     * Calculate triangle pattern values (the 10 squares in the 4 diagonals closest to the edge of the board).
     * <P>Two triangle pattern values are calculated from the 4 row patterns forming one half of the board.
     *
     * @return two triangle pattern values packed as one int; high 16 bits has the first pattern, low has the second pattern
     */
    public static int getConfigs(int config1, int config2, int config3, int config4) {
        return row1ToTriangle[config1] + row2ToTriangle[config2] + row3ToTriangle[config3] + row4ToTriangle[config4];
    }
}
