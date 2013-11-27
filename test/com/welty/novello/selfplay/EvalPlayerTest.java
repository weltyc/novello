package com.welty.novello.selfplay;

import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.BitBoard;
import com.welty.novello.solver.BitBoardUtils;
import junit.framework.TestCase;

/**
 */
public class EvalPlayerTest extends TestCase {
    public void test1PlySearch() throws Exception {
        final String evalName = "7B";
        final Eval eval = Players.eval(evalName);
        final EvalPlayer player = new EvalPlayer(eval, 1);

        final BitBoard prev = new BitBoard("--------\n" +
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
        final MoveScore moveScore = player.calcMove(prev, moves, -1);
        assertTrue("must be a legal move", BitBoardUtils.isBitSet(moves, moveScore.sq));

        final BitBoard terminal = prev.play(moveScore.sq);
        assertEquals(-terminal.eval(eval), moveScore.score);

        int score = simpleSearch(eval, prev, moves);
        assertEquals(score, moveScore.score);
    }

    // do a simple 1-ply search without sorting.
    private static int simpleSearch(Eval eval, BitBoard prev, long moves) {
        int score = Integer.MIN_VALUE;
        for (long m = moves; m!=0; ) {
            final int sq = Long.numberOfTrailingZeros(m);
            m&=~(1L<<sq);
            final BitBoard sub = prev.play(sq);
            final int subScore = -sub.eval(eval);
            if (subScore > score) {
                score = subScore;
            }
        }
        return score;
    }

    public void testSearchScoreWithPass() {
        final Eval eval = Players.eval("9A");
        final EvalPlayer player = new EvalPlayer(eval, 2);

        final BitBoard root = new BitBoard("--OO-O-O\n" +
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
        final BitBoard g1 = root.play("G1");
        final int subScore = -player.searchScore(g1.mover(), g1.enemy(), EvalPlayer.NO_MOVE, -3949, 1);
        // had a bug where it was returning the terminal value (+6) if the opponent passes. This position is way
        // better than that!
        assertTrue(subScore > 20* CoefficientCalculator.DISK_VALUE);
    }
}
