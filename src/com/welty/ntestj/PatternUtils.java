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

package com.welty.ntestj;

import com.orbanova.common.misc.Require;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: May 7, 2009
 * Time: 8:29:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatternUtils {
    public static final char[] nBase3s = {1, 3, 9, 27, 81, 243, 729, 2187, 6561, 3 * 6561, 9 * 6561};    // just base 3

    public static final int maxBase3PatternSize = nBase3s.length;

    private static final char[] outputStuff = "O.*".toCharArray();

    public static String PrintBase3(char pattern3, char size) {
        int i;
        char[] configString = new char[20];

        Require.gt(size, "size", 0);
        for (i = size - 1; i >= 0; i--) {
            configString[i] = outputStuff[pattern3 % 3];
            pattern3 /= 3;
        }
        configString[size] = 0;
        return new String(configString);
    }

    /**
     * convert a config into its length base-3 digits
     * one digit is stored per element of trits, so trits must have at least length elements
     * the least significant trit is stored in trits[0].
     */
    public static void ConfigToTrits(int config, int length, int[] trits) {
        int i;

        for (i = 0; i < length; i++) {
            trits[i] = config % 3;
            config /= 3;
        }
        Require.eq(config, "config", 0);
    }

    /**
     * change trits to config. trit[0] is smallest value
     * todo eliminate length as an argument, since we already know the length of trits
     */
    public static int TritsToConfig(int[] trits, int length) {
        int config;
        int i;

        config = 0;
        for (i = length; i > 0;)
            config = config * 3 + trits[--i];
        return config;
    }

    //  todo eliminate length as an argument, since we already know the length of trits
    public static int TritsToRConfig(int[] trits, int length) {
        int config = 0;
        for (int i = 0; i < length;)
            config = config * 3 + trits[i++];
        return config;
    }

    /**
     * Convert trits to config, where trits are in reverse order
     *
     * @param trits  array of trits
     * @param start  highest config trit is trits[start]
     * @param length lowest config trit is trits[start+length-1]
     * @return config base 3 representation of the chosen trits
     *         todo eliminate length as an argument, since we already know the length of trits
     */
    public static int TritsToRConfig(int[] trits, int start, int length) {
        int config = 0;
        for (int i = start; i < start + length;)
            config = config * 3 + trits[i++];
        return config;
    }

    public static char ReorderedConfig(char config, int nTrits, final int[] reordering) {
        int i;
        int[] trits = new int[20];

        Require.leq(nTrits, "nTrits", 20);
        ConfigToTrits(config, nTrits, trits);
        config = 0;
        for (i = reordering.length - 1; i >= 0; i--)
            config += config + config + trits[reordering[i]];

        return config;
    }
}
