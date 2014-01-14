package com.welty.novello.selfplay;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import junit.framework.TestCase;

public class EvalPlayerTest extends TestCase {
    public void test1PlySearch() throws Exception {
        final Eval eval = Players.currentEval();
        final EvalPlayer player = new EvalPlayer(eval, 1, "");

        final Position prev = Position.of("--------\n" +
                "--------\n" +
                "-----*--\n" +
                "---***--\n" +
                "---***--\n" +
                "---OO*--\n" +
                "------*-\n" +
                "--------\n" +
                "O");

        final long moves = prev.calcMoves();
        final MoveScore moveScore = player.calcMove(prev);
        assertTrue("must be a legal move", BitBoardUtils.isBitSet(moves, moveScore.sq));

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

    public void testNameChangeOnDepthChange() {
        final EvalPlayer evalPlayer = new EvalPlayer(Players.currentEval(), 1, "");
        assertEquals(Players.currentEval()+":1", evalPlayer.toString());
        evalPlayer.setMaxDepth(2);
        assertEquals(Players.currentEval()+":2", evalPlayer.toString());
    }

    /**
     * Solve value was 100x too high in Viewer, printing out "+400 disks" instead of "+4 disks".
     */
    public void testSolveValue() {
        final Eval eval = Players.currentEval();
        final EvalPlayer player = new EvalPlayer(eval, 1, "");

        final Position prev = Position.of("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*. O");

        assertEquals(6400, player.calcMove(prev).score);
    }
}
