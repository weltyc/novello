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

import static com.welty.ntestj.PatternUtils.ReorderedConfig;
import static com.welty.ntestj.PatternUtils.nBase3s;
import com.orbanova.common.misc.Require;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 7, 2009
 * Time: 9:16:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class R33Table {
    // size of unreverse table for various pattern sizes
    public static char Base3ToR33ID(char config) {
        return base3ToR33IDTable[config];
    }

    public static char R33IDToBase3(char id) {
        return r33IDToBase3Table[id];
    }


    private static final char[] base3ToR33IDTable = new char[3 * 6561];
    private static final char[] r33IDToBase3Table = new char[14 * 729];

    private static void InitR33IDTables() {
        char config, rconfig, id;

        // base3ToR33IDTable
        for (id = config = 0; config < nBase3s[9]; config++) {
            rconfig = R33Reverse(config);
            if (config <= rconfig) {
                base3ToR33IDTable[config] = base3ToR33IDTable[rconfig] = id;
                r33IDToBase3Table[id] = config;
                id++;
            }
        }
        Require.eq(id, "id", 14 * 729);
    }

    // new version
    private static final int R33ID[] = {0, 3, 6, 1, 4, 7, 2, 5, 8};

    static {
        InitR33IDTables();
    }

    private static char R33Reverse(char config) {
        return ReorderedConfig(config, 9, R33ID);
    }
}
