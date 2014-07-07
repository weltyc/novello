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

import com.welty.ntestj.CMap;
import static com.welty.ntestj.CMap.TIdType.kCRID;
import com.welty.ntestj.Utils;
import com.orbanova.common.misc.Require;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 8, 2009
 * Time: 5:41:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigToPotMobTable {
    public static final int[][][] configToPotMob = new int[2][9][];
    private static final int[][][] configToMob = new int[2][9][];
    public static final int[][] configToPotMobTriangle = new int[2][];
    private static final int[][] configToMobTriangle = new int[2][];

    private static int PotMob(byte black, byte empty, int length) {
        char mask;

        mask = (char) ((1 << length) - 1);
        empty &= mask;
        mask--;
        return Integer.bitCount(black & (empty << 1) & (mask >> 1)) + Integer.bitCount(black & (empty >> 1) & mask);
    }

    private static int Mob(byte black, byte empty, int length) {
        int col;
        byte mask, a, b, result;

        // check for west moves
        // remember bits are stored from left to right so '<<' moves to the right
        mask = 0x01;
        a = b = 0;
        result = 0;
        for (col = 0; col < length; col++) {
            result |= b & empty & mask;
            a = (byte) ((~empty) & (a | black));
            b = (byte) (a & ~black);
            a <<= 1;
            b <<= 1;
            mask <<= 1;
        }

        // check for east moves
        mask = (byte) (1 << (length - 1));
        a = b = 0;
        for (col = length - 1; col >= 0; col--) {
            result |= b & empty & mask;
            a = (byte) ((~empty) & (a | black));
            b = (byte) (a & ~black);
            a >>= 1;
            b >>= 1;
            mask >>= 1;
        }

        return Integer.bitCount(result);
    }

//	length-4 diagonal subconfiguration of a corner pattern

    private static int D4Subconfig(int config) {
        int subconfig;

        config /= 9;
        subconfig = config % 3;
        config /= 27;
        subconfig = (subconfig * 3) + config % 3;
        config /= 3;
        subconfig = (subconfig * 3) + config % 3;
        config /= 27;
        subconfig = (subconfig * 3) + config % 3;
        Require.lt(subconfig, "subconfig", 81);
        return subconfig;
    }

    private static void InitConfigToPotMob() {
        int length, nConfigs, config, subconfig, color;
        char base2;
        byte black, empty, white;

        // fill configToPotMob[0] with black mobility and configToPotMob[1] with white mobility
        for (length = 3, nConfigs = 27; length <= Utils.N; length++, nConfigs *= 3) {
            for (color = 0; color < 2; color++) {
                configToPotMob[color][length] = new int[nConfigs];
                configToMob[color][length] = new int[nConfigs];
            }
            for (config = 0; config < nConfigs; config++) {
                base2 = Base3ToBase2Table.base3ToBase2(config + 3280 - (nConfigs >> 1));
                black = (byte) (base2 >> 8);
                empty = (byte) (base2 & 0xFF);
                white = (byte) ~(empty | black);
                Require.eq(empty & black, "empty&black", 0);

                // pot mob
                configToPotMob[0][length][config] = PotMob(black, empty, length);
                configToPotMob[1][length][config] = PotMob(white, empty, length);

                // mob
                configToMob[0][length][config] = Mob(black, empty, length);
                configToMob[1][length][config] = Mob(white, empty, length);
            }
        }

        // fill configToPotMobTriangle with potential mobilities for G-corner patterns
        nConfigs = new CMap(kCRID, 10).NConfigs();
        configToPotMobTriangle[0] = new int[nConfigs];
        configToPotMobTriangle[1] = new int[nConfigs];
        configToMobTriangle[0] = new int[nConfigs];
        configToMobTriangle[1] = new int[nConfigs];
        for (config = 0; config < nConfigs; config++) {
            subconfig = D4Subconfig(config);
            configToPotMobTriangle[0][config] = configToPotMob[0][4][subconfig];
            configToPotMobTriangle[1][config] = configToPotMob[1][4][subconfig];
            configToMobTriangle[0][config] = configToMob[0][4][subconfig];
            configToMobTriangle[1][config] = configToMob[1][4][subconfig];
        }
    }

    static {
        InitConfigToPotMob();
    }
}