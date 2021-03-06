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

package com.welty.novello.eval;

/**
 * Utility routines dealing with base-3 numbers and associated patterns.
 */
public class Base3 {
    public static final char[] output = ".*O".toCharArray();

    private static final int[] base3FromBase2 = new int[1<<11];
    static {
        for (int mover = 0; mover<base3FromBase2.length; mover++) {
            int base3 = 0;
            final int nDigits = 32 - Integer.numberOfLeadingZeros(mover);
            for (int i = nDigits; i-- > 0; ) {
                base3 *= 3;
                base3 += ((mover>>i) & 1);
            }
            base3FromBase2[mover] = base3;
        }
    }

    public static int base2ToBase3(int mover, int enemy) {
//        int base3 = 0;
//        final int nDigits = 32 - Integer.numberOfLeadingZeros(mover | enemy);
//        for (int i = nDigits; i-- > 0; ) {
//            base3 *= 3;
//            base3 += ((mover>>i) & 1) | 2 * ((enemy>>i) & 1);
//        }
//        return base3;
        return base3FromBase2[mover] + 2*base3FromBase2[enemy];
    }

    /**
     * Reverse the base 3 digits of instance
     *
     * @param instance initial input
     * @return reversed instance
     */
    static int reverse(int instance, int digits) {
        int reverse = 0;
        for (int d=0; d<digits; d++) {
            reverse*=3;
            reverse += instance%3;
            instance /=3;
        }
        return reverse;
    }

    static int nInstances(int nDisks) {
        int nInstances = 1;
        for (int i=0; i<nDisks; i++) {
            nInstances*=3;
        }
        return nInstances;
    }

    /**
     * Generate oridDescription from one of the orid's instances
     *
     * @param instance instance. NOT orid.
     * @param nDisks number of disks.
     * @return oridDescription
     */
    static String description(int instance, int nDisks) {
        final StringBuilder sb = new StringBuilder();
        while (instance>0) {
            sb.append(output[instance%3]);
            instance /= 3;
        }
        while (sb.length()<nDisks) {
            sb.append(output[0]);
        }
        return sb.reverse().toString();
    }
}
