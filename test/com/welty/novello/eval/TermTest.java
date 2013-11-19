package com.welty.novello.eval;

import junit.framework.TestCase;

/**
 */
public class TermTest extends TestCase {
    public static final int FEATURE_1_N_ORIDS = 5;

    static final Feature feature1 = new SoloFeature() {

        @Override public int nOrids() {
            return FEATURE_1_N_ORIDS;
        }

        @Override public String oridDescription(int orid) {
            return "" + orid;
        }
    };

    static final Term term1 = new Term(feature1) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return (int) (mover % FEATURE_1_N_ORIDS);
        }
    };

    public void testOrid() {
        for (long mover = 0; mover < FEATURE_1_N_ORIDS; mover++) {
            final int instance = term1.instance(mover, 0, 0, 0);
            final int expected = feature1.orid(instance);
            assertEquals(expected, term1.orid(mover, 0, 0, 0));
        }
    }

    public void testGetFeature() {
        assertEquals(feature1, term1.getFeature());
    }
}
