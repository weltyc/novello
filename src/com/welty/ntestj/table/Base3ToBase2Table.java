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

import static com.welty.ntestj.table.Base2ToBase3Table.base2ToBase3;
import com.orbanova.common.misc.Require;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 3, 2009
 * Time: 10:49:42 AM
 * To change this template use File | Settings | File Templates.
 */
class Base3ToBase2Table {
    private final static char[] base3ToBase2Table = new char[6561];

    static {
        initBase3ToBase2Table();
    }

    private static void initBase3ToBase2Table() {
        // base3ToBase2Table, convert base 2 to base 3 (only for length 8)
        for (int empty = 0; empty < 256; empty++) {
            for (int black = 0; black < 256; black++) {
                if ((empty & black) == 0) {
                    final int config = base2ToBase3(black, empty);
                    Require.lt(config, "config", base3ToBase2Table.length);
                    base3ToBase2Table[config] = (char) ((black << 8) | empty);
                }
            }
        }
    }

    public static char base3ToBase2(int base2) {
        return base3ToBase2Table[base2];
    }
}
