package com.welty.novello.eval;

import com.orbanova.common.misc.Require;

/**
 * An edge, plus X-squares and center disks flag. See Edge2XTerm.
 */
class Edge3XFeature extends MultiFeature {
    public static Edge3XFeature instance = new Edge3XFeature(new OridCalc());

    private Edge3XFeature(OridCalc calc) {
        super("edge3X", calc.orids, calc.descriptions);
    }

    private static class OridCalc {
        /**
         * When reflecting the pieces, where do they go?
         */
        private static final int[] reversal = {7, 6, 5, 4, 3, 2, 1, 0, 10, 9, 8};

        /**
         * Digits, base 3.
         * <p/>
         * trits[0] = least significant trit, and is also the integer representing the piece in the corner.
         */
        int[] trits = new int[11];

        /**
         * During construction, number of orids used so far (and also the index of the next orid).
         */
        private int orid;

        /**
         * orid from feature
         */
        int[] orids = new int[3 * 9 * 6561];

        /**
         * Description from orid.
         */
        String[] descriptions = new String[88938];

        OridCalc() {
            update(10);
            Require.eq(descriptions.length, "Expected orids", orid);
        }

        private void update(int index) {
            if (index < 0) {
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
                if (i == 8) {
                    sb.append('/');
                }
            }
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
