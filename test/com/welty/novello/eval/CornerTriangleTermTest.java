package com.welty.novello.eval;

import com.welty.novello.core.Me;
import com.welty.novello.core.Board;
import junit.framework.TestCase;

import java.util.Arrays;

import static com.welty.novello.core.BitBoardUtils.reflectHorizontally;
import static com.welty.novello.core.BitBoardUtils.reflectVertically;
import static org.junit.Assert.assertArrayEquals;

/**
 */
public class CornerTriangleTermTest extends TestCase {
    public void testInstance() {
        testInstance(0, 0, 0);
        testInstance(1, 1, 0);
        testInstance(2, 0, 1);
        testInstance(3, 2, 0);
        testInstance(6, 0, 2);
        testInstance(6, 0, 0xFFFFFFFFFEFCF8F2L);
        testInstance(9, 4, 0);
        testInstance(27, 8, 0);
        testInstance(81, 0x0100, 0);
        testInstance(243, 0x0200, 0);
        testInstance(729, 0x0400, 0);
        testInstance(2187, 0x010000, 0);
        testInstance(6561, 0x020000, 0);
        testInstance(3*6561, 0x1000000, 0);
    }

    private void testInstance(int instance, long mover, long enemy) {
        testInstance(false, false, instance, mover, enemy);
        testInstance(false, true, instance, reflectHorizontally(mover), reflectHorizontally(enemy));
        testInstance(true, false, instance, reflectVertically(mover), reflectVertically(enemy));
        testInstance(true, true, instance, Long.reverse(mover), Long.reverse(enemy));
    }

    private void testInstance(boolean top, boolean left, int instance, long mover, long enemy) {
        final CornerTriangleTerm term = new CornerTriangleTerm(top, left);
        assertEquals(top + "-" + left, instance, term.instance(mover, enemy, 0, 0));
    }

    public void testReflectionOrids() {
        testReflectionOrids(Me.early);
        testReflectionOrids(Me.late);
    }

    private static void testReflectionOrids(Me tp) {
        final Board board = tp.toPosition();
        int[] expected = calcOrids(board, 0);
        for (int r=1; r<8; r++) {
            assertArrayEquals(""+r, expected, calcOrids(board, r));
        }
    }

    private static int[] calcOrids(Board board, int r) {
        int[] expected = new int[4];
        Term[] terms = {
                new CornerBlockTerm(false, false),
                new CornerBlockTerm(false, true),
                new CornerBlockTerm(true, false),
                new CornerBlockTerm(true, true)
        };
        final Board reflection = board.reflection(r);
        for (int i=0; i<4; i++) {
            expected[i]=terms[i].orid(reflection.mover(), reflection.enemy(), 0, 0);
        }
        Arrays.sort(expected);
        return expected;
    }
}
