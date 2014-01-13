package com.welty.ntestj.table;

import static com.welty.ntestj.PatternUtils.ReorderedConfig;
import static com.welty.ntestj.PatternUtils.nBase3s;
import com.orbanova.common.misc.Require;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 7, 2009
 * Time: 9:02:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class CridTable {
    public static final char[] nCRIDs = {0, 0, 0, 0, 0, 0, 405, 0, 0, 0, 9 * 3321};                // corner reversal
    public static final int maxCRIDPatternSize = nCRIDs.length;
    private static final char[][] base3ToCRIDTable = new char[maxCRIDPatternSize][];
    private static final char[][] cRIDToBase3Table = new char[maxCRIDPatternSize][];

    private static void InitCRIDTables() {
        int size;
        char config, rconfig, id;

        for (size = 0; size < maxCRIDPatternSize; size++) {
            if (nCRIDs[size] != 0) {
                base3ToCRIDTable[size] = new char[nBase3s[size]];
                cRIDToBase3Table[size] = new char[nCRIDs[size]];
            }
        }

        // base3ToCRIDTable, corner flips
        for (id = config = 0; config < nBase3s[6]; config++) {
            rconfig = CRID6Reverse(config);
            if (config <= rconfig) {
                base3ToCRIDTable[6][config] = base3ToCRIDTable[6][rconfig] = id;
                cRIDToBase3Table[6][id] = config;
                id++;
            }
        }
        Require.eq(id, "id", nCRIDs[6]);
        for (id = config = 0; config < nBase3s[10]; config++) {
            rconfig = CRID10Reverse(config);
            if (config <= rconfig) {
                base3ToCRIDTable[10][config] = base3ToCRIDTable[10][rconfig] = id;
                cRIDToBase3Table[10][id] = config;
                id++;
            }
        }
        Require.eq(id, "id", nCRIDs[10]);
    }

    private static final int[] CRID6 = {2, 1, 0, 4, 3, 5};
    private static final int[] CRID10 = {0, 1, 9, 8, 7, 6, 5, 4, 3, 2};

    private static char CRID6Reverse(char config) {
        return ReorderedConfig(config, 6, CRID6);
    }

    private static char CRID10Reverse(char config) {
        return ReorderedConfig(config, 10, CRID10);
    }

    public static char Base3ToCRID(char config, char size) {
        return base3ToCRIDTable[size][config];
    }

    public static char CRIDToBase3(char id, char size) {
        return cRIDToBase3Table[size][id];
    }

    static {
        InitCRIDTables();
    }
}
