package com.welty.novello.eval;

import junit.framework.TestCase;

/**
 */
public class Base3Test extends TestCase {
    public void testBase2ToBase3() {
        testBase2ToBase3(0, 0, 0);
        testBase2ToBase3(1, 0, 1);
        testBase2ToBase3(0, 1, 2);
        testBase2ToBase3(2, 0, 3);
        testBase2ToBase3(3, 0, 4);
        testBase2ToBase3(2, 1, 5);
        testBase2ToBase3(0, 2, 6);
    }

    private void testBase2ToBase3(int mover, int enemy, int expected) {
        assertEquals(expected, Base3.base2ToBase3(mover, enemy));
    }
}
