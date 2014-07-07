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

import com.orbanova.common.misc.Require;

/**
 * A 4x4 triangle in the corner of the board.
 *
 * Disk indices:
 * 0 1 2 3
 * 4 5 6
 * 7 8
 * 9
 */
class CornerTriangleFeature extends MultiFeature {
    public static CornerTriangleFeature instance = new CornerTriangleFeature(new OridCalc());

    private CornerTriangleFeature(OridCalc calc) {
        super("Corner Triangle", calc.orids, calc.descriptions);
    }

    private static class OridCalc {
        /**
         * When reflecting the pieces, where do they go?
         */
        private static final int[] reversal = {0, 4, 7, 9, 1, 5, 8, 2, 6, 3};

        /**
         * Digits, base 3.
         *
         * trits[0] = least significant trit, and is also the integer representing the piece in the corner.
         */
        int[] trits = new int[10];

        /**
         * During construction, number of orids used so far (and also the index of the next orid).
         */
        private int orid;

        /**
         * orid from feature
         */
        int[] orids = new int[9*6561];

        /**
         * Description from orid.
         */
        String[] descriptions = new String[29889];

        OridCalc() {
            update(9);
            Require.eq(descriptions.length, "Expected orids", orid);
        }

        private void update(int index) {
            if (index<0) {
                final int instance = instance(trits);
                final int rInstance = rInstance(trits);
                if (instance <= rInstance) {
                    descriptions[orid] = description(trits);
                    orids[instance] = orids[rInstance] = orid;
                    orid++;
                }
            } else {
                for (int trit = 0; trit < 3; trit++) {
                    trits[index] = trit;
                    update(index - 1);
                }
            }
        }

        private static String description(int[] trits) {
            final StringBuilder sb = new StringBuilder();
            for (int i = trits.length; i-- > 0; ) {
                sb.append(Base3.output[trits[i]]);
                if (i==9 || i==7 || i==4) {
                    sb.append('/');
                }
            }
            sb.append(" <-- corner");
            return sb.toString();
        }

        private int instance(int[] trits) {
            int instance = 0;
            for (int i = trits.length; i-- > 0; ) {
                instance = instance * 3 + trits[i];
            }
            return instance;
        }

        private int rInstance(int[] trits) {
            int instance = 0;
            for (int i = trits.length; i-- > 0; ) {
                instance = instance * 3 + trits[reversal[i]];
            }
            return instance;
        }
    }
}
