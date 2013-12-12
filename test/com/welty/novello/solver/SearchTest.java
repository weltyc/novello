package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Counts;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.DiskEval;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.Players;
import junit.framework.TestCase;

/**
 */
public class SearchTest extends TestCase {
    public void test1PlySearch() throws Exception {
        final Eval eval = Players.currentEval();
        final Counter counter = new Counter(eval);
        final Search search = new Search(counter, 0);

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
        final MoveScore moveScore = search.calcMove(prev, moves, 1, false);
        assertTrue("must be a legal move", BitBoardUtils.isBitSet(moves, moveScore.sq));
        assertEquals(Long.bitCount(moves), counter.nFlips());

        final Position terminal = prev.play(moveScore.sq);
        assertEquals(-eval.eval(terminal), moveScore.score);

        int score = simpleSearch(eval, prev, moves);
        assertEquals(score, moveScore.score);
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

    public void testSearchScoreWithPass() {
        final Eval eval = Players.currentEval();
        final Search search = new Search(new Counter(eval), 0);

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
        final int subScore = -search.calcScore(g1, 1, false);
        // had a bug where it was returning the terminal value (+6) if the opponent passes. This position is way
        // better than that!
        assertTrue(subScore > 20 * CoefficientCalculator.DISK_VALUE);
    }

    public void testTreeMove() {
        final Eval eval = new DiskEval();
        final Search search = new Search(new Counter(eval), 0);
        final Position position = Position.of("-------- -------- -------- --OO---- --*O*--- ----OO-- -------- -------- *");
        final long mover = position.mover();
        final long enemy = position.enemy();
        final long moverMoves = position.calcMoves();

        // so the tests are readable
        final int c3 = BitBoardUtils.textToSq("C3");
        final int e3 = BitBoardUtils.textToSq("E3");
        final int g7 = BitBoardUtils.textToSq("G7");

        // true value is within the window
        Search.BA ba = search.treeMove(mover, enemy, moverMoves, -6400, 6400, 1, false);
        assertEquals(200, ba.score);
        assertEquals(c3, ba.bestMove);

        // true value is above the window
        ba = search.treeMove(mover, enemy, moverMoves, -6400, 80, 1, false);
        assertTrue(100 <= ba.score);
        assertTrue(ba.score <= 200);
        assertTrue(ba.bestMove == c3 || ba.bestMove == e3 || ba.bestMove == g7);

        // true value is at the bottom of the window
        ba = search.treeMove(mover, enemy, moverMoves, 200, 6400, 1, false);
        assertEquals(200, ba.score);
        assertEquals(-1, ba.bestMove);

        // true value is below the window
        ba = search.treeMove(mover, enemy, moverMoves, 300, 6400, 1, false);
        assertEquals(200, ba.score); // required by fail-soft. Fail-hard would return 300.
        assertEquals(-1, ba.bestMove);
    }

    public void testMpc() {
        for (int depth = 4; depth <= 6; depth+=2) {
            // simple test: 4 ply search should give the same result with fewer nodes
            final Eval eval = Players.currentEval();
            final Search search = new Search(new Counter(eval), 0);
            final Position position = Position.of("-------- -------- -------- --OOO--- --*O*--- ----OO-- -------- -------- *");
            final int score = search.calcScore(position, depth, false);
            final long n = search.counts().nFlips;
            final int mpcScore = search.calcScore(position, depth, true);
            final long nMpc = search.counts().nFlips - n;
            assertEquals(score, mpcScore);
            assertTrue("MPC should give the same result with fewer nodes", nMpc < n);
            System.out.println("MPC used " + nMpc + ", full-width used " + n);
        }
    }

    public void testFlipToEvalRatio() {
        final Eval eval = Players.currentEval();
        final Search search = new Search(new Counter(eval), 0);
        final int depth = 4;

        Position position = Position.START_POSITION;
        search.calcMove(position, position.calcMoves(), depth, false);
        final Counts counts = search.counts();
        final long nFlips = counts.nFlips;
        final long nEvals = counts.nEvals;
        System.out.format("%,d flips and %,d evals", nFlips, nEvals);
        assertTrue(nFlips < nEvals * 2);
    }
}
