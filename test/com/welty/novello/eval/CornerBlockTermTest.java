package com.welty.novello.eval;

import com.orbanova.common.misc.ArrayTestCase;
import com.welty.novello.solver.BitBoard;
import junit.framework.TestCase;

import java.util.Arrays;

import static com.welty.novello.solver.BitBoardUtils.reflectHorizontally;
import static com.welty.novello.solver.BitBoardUtils.reflectVertically;

/**
 */
public class CornerBlockTermTest extends ArrayTestCase {
    public void testInstance() {
        testInstance(0, 0, 0);
        testInstance(1, 1, 0);
        testInstance(2, 0, 1);
        testInstance(6, 0, 2);
        testInstance(6, 0, 0xFFFFFFFFFFF8F8FAL);
        testInstance(27, 0x0100, 0);
        testInstance(81, 0x0200, 0);
        testInstance(243, 0x0400, 0);
        testInstance(729, 0x010000, 0);
        testInstance(2187, 0x020000, 0);
        testInstance(6561, 0x040000, 0);
    }

    private void testInstance(int instance, long mover, long enemy) {
        testInstance(false, false, instance, mover, enemy);
        testInstance(false, true, instance, reflectHorizontally(mover), reflectHorizontally(enemy));
        testInstance(true, false, instance, reflectVertically(mover), reflectVertically(enemy));
        testInstance(true, true, instance, Long.reverse(mover), Long.reverse(enemy));
    }

    private void testInstance(boolean top, boolean left, int instance, long mover, long enemy) {
        final CornerBlockTerm term = new CornerBlockTerm(top, left);
        assertEquals(top + "-" + left, instance, term.instance(mover, enemy, 0, 0));
    }

    public void testReflectionOrids() {
        testReflectionOrids(EvalStrategyTest.sparsePosition);
        testReflectionOrids(EvalStrategyTest.densePosition);
    }

    private static void testReflectionOrids(BitBoard position) {
        int[] expected = calcOrids(position, 0);
        for (int r=1; r<8; r++) {
            assertEquals(""+r, expected, calcOrids(position, r));
        }
    }

    private static int[] calcOrids(BitBoard bitBoard, int r) {
        int[] expected = new int[4];
        Term[] terms = {
                new CornerBlockTerm(false, false),
                new CornerBlockTerm(false, true),
                new CornerBlockTerm(true, false),
                new CornerBlockTerm(true, true)
        };
        final BitBoard reflection = bitBoard.reflection(r);
        for (int i=0; i<4; i++) {
            expected[i]=terms[i].orid(reflection.mover(), reflection.enemy(), 0, 0);
        }
        Arrays.sort(expected);
        return expected;
    }
}
