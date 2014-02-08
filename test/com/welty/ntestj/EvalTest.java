package com.welty.ntestj;

import com.welty.novello.core.Me;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.ntest.NBoardSyncEngine;
import com.welty.othello.gui.ExternalEngineManager;
import junit.framework.TestCase;

/**
 */
public class EvalTest extends TestCase {
    /**
     * Try to match ntestJ evaluation of the position with 59 empties
     * <p/>
     * <p/>
     * A B C D E F G H
     * 1 - - - - - - - -  1
     * 2 - - - - - - - -  2
     * 3 - - - - - - - -  3
     * 4 - - - O * - - -  4
     * 5 - - - * * * - -  5
     * 6 - - - - - - - -  6
     * 7 - - - - - - - -  7
     * 8 - - - - - - - -  8
     * A B C D E F G H
     * Black: 4  White: 1  Empty: 59
     * White to move
     * <p/>
     * Rows & cols done. Value so far: 47. PotMobs 2/8
     * Diagonal 5 configs : 121, 121, 121, 121
     * Diagonal 5 done. Value so far: 47. PotMobs 2/8
     * Diagonal 6 done. Value so far: 64. PotMobs 2/10
     * Straight lines done. Value so far: 76. PotMobs 5/19
     * Done
     * Config: 29524, Id: 22306, Value:    1 (pms  0,  0)
     * Config: 29524, Id: 22306, Value:    1 (pms  0,  0)
     * Config: 29524, Id: 22306, Value:    1 (pms  0,  0)
     * Config: 29524, Id: 22306, Value:    1 (pms  0,  0)
     * Triangles done. Value so far: 80.
     * Raw pot mobs: 5, 19
     * Config:     9, Id:     9, Value:  -36
     * Config:     2, Id:     2, Value:    9
     * Potential mobility done. Value so far: 53.
     * Config: 29524, Id: 29524, Value:    2
     * Config: 29524, Id: 29524, Value:    2
     * Config: 29524, Id: 22201, Value:    2
     * Config: 29524, Id: 29524, Value:    2
     * Config: 29524, Id: 29524, Value:    2
     * Config: 29524, Id: 22201, Value:    2
     * Config: 29524, Id: 29524, Value:    2
     * Config: 29524, Id: 29524, Value:    2
     * Config: 29524, Id: 22201, Value:    2
     * Config: 29524, Id: 29524, Value:    2
     * Config: 29524, Id: 29524, Value:    2
     * Config: 29524, Id: 22201, Value:    2
     * Corners done. Value so far: 77.
     * Config:     3, Id:     3, Value:   -7
     * Config:     3, Id:     3, Value:    9
     * Mobility done. Value so far: 79.
     * Config:     1, Id:     1, Value:    2
     * Total Value= 81
     * 81
     */
    /**
     * Check that ntest's eval matches ntestJ's eval.
     * <p/>
     * If ntest is not available on this computer, ignore.
     */
    public void test1Ply() {

        if (ExternalEngineManager.getXei("ntest") == null) {
            System.out.println("ntest not available on this machine, skipping test");
            return;
        }
        final NBoardSyncEngine ntest = new NBoardSyncEngine("ntest", true);
        final CEvaluatorJ eval = CEvaluatorJ.getInstance();

        test1Ply(ntest, eval, Position.START_POSITION);
        test1Ply(ntest, eval, Me.early.toPosition());

        // don't test late position - ntest solves instead of giving an eval.
//        test1Ply(ntest, eval, Me.late.toPosition());
    }

    private static void test1Ply(NBoardSyncEngine ntest, CEvaluatorJ eval, Position pos) {
        final MoveScore moveScore = ntest.calcMove(pos, 1);
        final Position next = pos.play(moveScore.sq);
        final int value = eval.ValueJMobs(next.mover(), next.enemy(), next.calcMoves(), next.enemyMoves());
        assertEquals(-moveScore.centidisks, value);
    }
}
