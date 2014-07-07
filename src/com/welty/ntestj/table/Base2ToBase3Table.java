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

import com.orbanova.common.misc.Require;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 2, 2009
 * Time: 10:45:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class Base2ToBase3Table {
    /////////////////////////////////////////////////////////////
    // Compression from base 4 to base 3
    /////////////////////////////////////////////////////////////

    private final static short[] base2ToBase3Table = new short[256];
    private final static short[] base2ToBase3FlipTable = new short[256];

    static {
        initBase2ToBase3Tables();
    }

    public static int base2ToBase3(int black, int empty) {
        return (base2ToBase3Table[black] << 1) + base2ToBase3Table[empty];
    }

    public static int base2ToBase3(short config2) {
        return base2ToBase3(config2 >> 8, config2);
    }

    private static void initBase2ToBase3Tables() {
        int result, resultr;

        // base2ToBase3Table, convert base 3 to base 2, and
        //	base2ToBase3FlipTable, convert base 3 (backwards) to base 2
        for (int i = 0; i < 256; i++) {
            int mask = 0x80;
            int maskr = 0x01;
            resultr = result = 0;
            while (mask != 0) {
                result += result << 1;
                resultr += resultr << 1;
                if ((mask & i) != 0)
                    result++;
                if ((maskr & i) != 0)
                    resultr++;
                mask >>= 1;
                maskr <<= 1;
            }
            Require.leq(result, "result", 1 + 3 + 9 + 27 + 81 + 243 + 729 + 2187);
            base2ToBase3Table[i] = (short) result;
            base2ToBase3FlipTable[i] = (short) resultr;
        }
    }
}
