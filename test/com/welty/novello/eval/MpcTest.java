package com.welty.novello.eval;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class MpcTest extends TestCase {
    public void testCutterConstructor() {
        final ArrayList<int[]> ints = new ArrayList<>();
        // deep = 2*shallow + 1
        ints.add(new int[]{2, 5});
        ints.add(new int[]{0, 1});

        final Mpc.Cutter cutter = new Mpc.Cutter(ints, 1, 0);
        assertEquals(2, cutter.shallowScore(5));
        assertEquals(0, cutter.shallowScore(1));
    }

    public void testSliceConstructor() {
        // if we have no data we should still get MPC stats.
        final Mpc.Slice slice = new Mpc.Slice(15, Arrays.<int[]>asList());
        assertEquals(1, slice.cutters[3].length);
    }

    public void testApproximateSd() {
        // exact formula can change, but approximate sd should be in some sort of reasonable range.
        // actual value for evaluator c4s: 6.397
        final double sdDisks = Mpc.Cutter.approximateSd(5, 2, 0) / CoefficientCalculator.DISK_VALUE;
        assertTrue(sdDisks > 3);
        assertTrue(sdDisks < 12);
    }
}
