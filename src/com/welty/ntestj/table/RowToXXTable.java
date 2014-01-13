package com.welty.ntestj.table;

import com.welty.ntestj.PatternUtils;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 9, 2009
 * Time: 7:48:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class RowToXXTable {
    private static final int[] row2ToXX = new int[6561];

    static {
        final int[] trits = new int[8];
        // row 2 -> two X-square translator
        for (char config = 0; config < 6561; config++) {
            PatternUtils.ConfigToTrits(config, 8, trits);
            row2ToXX[config] = trits[1] + 3 * 6561 * trits[6];
        }
    }

    public static int getConfig(int edgeConfig, int insideConfig) {
        return edgeConfig + (edgeConfig << 1) + row2ToXX[insideConfig];
    }
}
