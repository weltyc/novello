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

package com.welty.ntestj.table;

import static com.welty.ntestj.PatternUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 9, 2009
 * Time: 7:09:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class RowTo2x5Table {
    /**
     * High 16 bits : config of left side of board
     * Low 16 bits: config of right side of board
     * row2 values are multiplied by 3^5
     */
    private static final int[] row2To2x5 = new int[6561], row1To2x5 = new int[6561];

    private static void init() {
        int[] trits = new int[8];
        // edge->2x5 translator
        for (char config = 0; config < 6561; config++) {
            ConfigToTrits(config, 8, trits);
            final int value1 = TritsToConfig(trits, 5);
            final int value2 = TritsToRConfig(trits, 3, 5);
            final int value = (value1 << 16) + value2;
            row2To2x5[config] = value;
            row1To2x5[config] = value * 243;
        }
    }

    public static int getConfigs(int config1, int config2) {
        return row1To2x5[config1] + row2To2x5[config2];
    }

    static {
        init();
    }
}
