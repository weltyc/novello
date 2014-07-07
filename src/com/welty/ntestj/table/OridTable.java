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
import com.orbanova.common.misc.Require;

/**
 * 'ORID' means the id# of a pattern. The IDs of two patterns are the same iff they are order reversals.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 7, 2009
 * Time: 8:40:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class OridTable {
    public static final char[] nORIDs = {1, 3, 6, 18, 45, 135, 378, 1134, 3321, 9963, 29646};    // order reversal
    public static final int maxORIDPatternSize = nORIDs.length;
    private static final char[][] oRIDToBase3Table = new char[maxORIDPatternSize][];
    private static final char[][] base3ToORIDTable = new char[maxORIDPatternSize][];

    public static char Base3ToORID(char config, char size) {
        return base3ToORIDTable[size][config];
    }

    public static char ORIDToBase3(char id, char size) {
        return oRIDToBase3Table[size][id];
    }

    static String PrintORID(char orid, char size) {
        Require.inRange("ORID must be in range", size, "size", 1, maxORIDPatternSize - 1);
        return PrintBase3(oRIDToBase3Table[size][orid], size);
    }


    private static char ORIDReverse(char config, int size) {
        Require.gt(size, "size", 0);
        int[] trits = new int[maxBase3PatternSize];

        ConfigToTrits(config, size, trits);
        // todo when is the appropriate time to casts this
        return (char) TritsToRConfig(trits, size);
    }


    private static void InitORIDTables() {
        int size;
        char config, rconfig, id;

        // allocate memory for identification tables
        for (size = 1; size < maxORIDPatternSize; size++) {
            base3ToORIDTable[size] = new char[nBase3s[size]];
            oRIDToBase3Table[size] = new char[nORIDs[size]];
        }
        // base3ToORIDTable, *OOOO and OOOO* have the same ORID
        for (size = 1; size < maxORIDPatternSize; size++) {
            for (id = config = 0; config < nBase3s[size]; config++) {
                rconfig = ORIDReverse(config, size);
                if (config <= rconfig) {
                    base3ToORIDTable[size][config] = base3ToORIDTable[size][rconfig] = id;
                    oRIDToBase3Table[size][id] = config;
                    id++;
                }
            }
            Require.eq(id, "id", nORIDs[size]);
        }
    }

    static {
        InitORIDTables();
    }
}
