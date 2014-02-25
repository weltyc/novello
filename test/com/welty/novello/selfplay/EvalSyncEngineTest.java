package com.welty.novello.selfplay;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class EvalSyncEngineTest extends TestCase {
    public void test1PlySearch() throws Exception {
        final Eval eval = Players.currentEval();
        final EvalSyncEngine engine = new EvalSyncEngine(eval, "");

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
        final MoveScore moveScore = engine.calcMove(prev, 1);
        assertTrue("must be a legal move", BitBoardUtils.isBitSet(moves, moveScore.sq));

        final Position terminal = prev.play(moveScore.sq);
        assertEquals(-eval.eval(terminal), moveScore.centidisks);

        int score = simpleSearch(eval, prev, moves);
        assertEquals(score, moveScore.centidisks);
    }

    // do a simple 1-ply search without sorting.
    private static int simpleSearch(Eval eval, Position prev, long moves) {
        int score = Integer.MIN_VALUE;
        for (long m = moves; m != 0; ) {
            final int sq = Long.numberOfTrailingZeros(m);
            m &= ~(1L << sq);
            final Position sub = prev.play(sq);
            final int subScore = -eval.eval(sub);
            if (subScore > score) {
                score = subScore;
            }
        }
        return score;
    }

    /**
     * Solve value was 100x too high in Viewer, printing out "+400 disks" instead of "+4 disks".
     */
    public void testSolveValue() {
        final Eval eval = Players.currentEval();
        final EvalSyncEngine player = new EvalSyncEngine(eval, "");

        final Position prev = Position.of("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO*. O");

        assertEquals(6400, player.calcMove(prev, 1).centidisks);
    }

    public void testCalcSearchDepth() {
        final EvalSyncEngine engine = new EvalSyncEngine(Players.currentEval(), "NS");
        assertEquals(6, engine.calcSearchDepth(Position.START_POSITION, 6).searchDepth);
    }

    public void testInsertSorted() {
        ArrayList<MoveScore> moveScores = new ArrayList<>();
        final MoveScore m30 = new MoveScore(30, 30);
        final MoveScore m20 = new MoveScore(20, 20);
        final MoveScore m10 = new MoveScore(10, 10);
        moveScores.add(m30);
        moveScores.add(m20);
        moveScores.add(m10);
        final MoveScore m15 = new MoveScore(20, 15);
        EvalSyncEngine.insertSorted(moveScores, 1, m15);
        assertEquals(Arrays.asList(m30, m15, m10), moveScores);

        final MoveScore m40 = new MoveScore(10, 40);
        EvalSyncEngine.insertSorted(moveScores, 2, m40);
        assertEquals(Arrays.asList(m40, m30, m15), moveScores);
    }
}
