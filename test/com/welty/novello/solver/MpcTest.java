package com.welty.novello.solver;

import com.welty.novello.eval.Mpc;
import junit.framework.TestCase;

import java.util.ArrayList;

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
}
