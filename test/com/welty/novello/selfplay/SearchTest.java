package com.welty.novello.selfplay;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Eval;
import junit.framework.TestCase;

/**
 */
public class SearchTest extends TestCase {
    public void test1PlySearch() throws Exception {
        final Eval eval = Players.eval("b1");
        final Search search = new Search(eval, 1);

        final Position prev = Position.of("--------\n" +
                "--------\n" +
                "-----*--\n" +
                "---***--\n" +
                "---***--\n" +
                "---OO*--\n" +
                "------*-\n" +
                "--------\n" +
                "O");

        System.out.println(prev);

        final long moves = prev.calcMoves();
        final MoveScore moveScore = search.calcMove(prev, moves, 1);
        assertTrue("must be a legal move", BitBoardUtils.isBitSet(moves, moveScore.sq));
        assertEquals(Long.bitCount(moves), search.nFlips());

        final Position terminal = prev.play(moveScore.sq);
        assertEquals(-eval.eval(terminal), moveScore.score);

        int score = simpleSearch(eval, prev, moves);
        assertEquals(score, moveScore.score);
    }

    // do a simple 1-ply search without sorting.
    private static int simpleSearch(Eval eval, Position prev, long moves) {
        int score = Integer.MIN_VALUE;
        for (long m = moves; m!=0; ) {
            final int sq = Long.numberOfTrailingZeros(m);
            m&=~(1L<<sq);
            final Position sub = prev.play(sq);
            final int subScore = -eval.eval(sub);
            if (subScore > score) {
                score = subScore;
            }
        }
        return score;
    }

    public void testSearchScoreWithPass() {
        final Eval eval = Players.eval("b1");
        final Search search = new Search(eval, 1);

        final Position root = Position.of("--OO-O-O\n" +
                "--****OO\n" +
                "*--*OOOO\n" +
                "-***OOOO\n" +
                "-***O*OO\n" +
                "-*OO**-O\n" +
                "*****-*-\n" +
                "OOOOOO-*\n" +
                "O");
        // g1  pass e1 is the best line

//        player.calcMove(root, root.calcMoves(), -1);
        final Position g1 = root.play("G1");
        final int subScore = -search.calcScore(g1, 1);
        // had a bug where it was returning the terminal value (+6) if the opponent passes. This position is way
        // better than that!
        assertTrue(subScore > 20* CoefficientCalculator.DISK_VALUE);
    }
}
