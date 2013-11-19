package com.welty.novello.eval;

import junit.framework.TestCase;

/**
 */
public class TermTest extends TestCase {
    static final Feature feature1 = new SoloFeature("orid 1", "orid 2", "orid 3");
    static final Feature feature2 = new MultiFeature(new int[]{0,1,0}, new String[]{"orid 0", "orid 1"});

    /**
     * A sample term whose instance is mover%3 and whose orid is mover%3
     */
    static final Term term1 = new Term(feature1) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return (int) (mover % feature1.nInstances());
        }
    };

    /**
     * A sample term whose instance is mover%3 and whose orid is instance==1?1:0
     */
    static final Term term2 = new Term(feature2) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return (int) (mover % feature2.nInstances());
        }
    };

    public void testOrid() {
        for (long mover = 0; mover < feature1.nInstances(); mover++) {
            final int instance = term1.instance(mover, 0, 0, 0);
            final int expected = feature1.orid(instance);
            assertEquals(expected, term1.orid(mover, 0, 0, 0));
        }
        for (long mover = 0; mover < feature2.nInstances(); mover++) {
            final int instance = term2.instance(mover, 0, 0, 0);
            final int expected = feature2.orid(instance);
            assertEquals(expected, term2.orid(mover, 0, 0, 0));
        }
    }

    public void testGetFeature() {
        assertEquals(feature1, term1.getFeature());
        assertEquals(feature2, term2.getFeature());
    }
}
