package com.welty.novello.selfplay;

import com.welty.novello.eval.PositionValue;
import com.welty.novello.core.Position;
import junit.framework.TestCase;

import java.util.List;

/**
 */

public class MutableGameTest extends TestCase {

    private static final Position START_POSITION = new Position(0x0000000810000000L, 0x0000001008000000L, true);

    public void testUpdates() {
        final Position startPosition = START_POSITION;
        final MutableGame game = new MutableGame(startPosition, "Boris", "William","VistaNova");
        assertEquals(START_POSITION, game.getStartPosition());
        assertEquals(START_POSITION, game.getLastPosition());
        assertTrue(game.toGgf().contains("BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]"));
        assertTrue(game.toGgf().contains("PC[VistaNova]"));
        assertTrue(game.toGgf().contains("TY[8r]"));
        assertTrue(game.toGgf().contains("RE[?]"));
        assertTrue(game.toGgf().contains("TI["));
        assertTrue(game.toGgf().contains("PB[Boris]"));
        assertTrue(game.toGgf().contains("PW[William]"));
        assertTrue(game.toGgf().startsWith("(;GM[Othello]"));
        assertTrue(game.toGgf().endsWith(";)"));

        game.play("F5");
        final Position nextPosition = new Position(0x000000081C000000L, 0x0000001000000000L, false);
        assertEquals(START_POSITION, game.getStartPosition());
        assertEquals(nextPosition, game.getLastPosition());
        assertTrue(game.toGgf().contains("BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]"));
        assertTrue(game.toGgf().contains("B[F5]"));

        game.play("D6//1.03");
        assertTrue(game.toGgf().contains("W[D6//1.03]"));

        //
        // we're going to cheat and assume black passes here, to test passing code and to make the game shorter.
        //

        game.play("PASS");
        game.play("F4");
        game.play("PASS");
        game.play("F6");
        System.out.println(game.toGgf());
        assertTrue(game.toGgf().contains("B[PASS]W[F4]B[PASS]W[F6]"));
        assertFalse("no final pass", game.toGgf().contains("B[PASS]W[F4]B[PASS]W[F6]B["));

        final List<PositionValue> pvs = game.calcPositionValues();
        assertEquals(4, pvs.size());
        final boolean[] blackToMoves = {true, false, false, false};
        assertEquals(8, pvs.get(1).value);
        assertEquals(8, pvs.get(2).value);
        assertEquals(8, pvs.get(3).value);
        for (int i=0; i<4; i++) {
            final PositionValue pv = pvs.get(i);
            assertEquals(60 - i, pv.nEmpty());
            final boolean blackToMove =blackToMoves[i];
            final int netScore = blackToMove ? -8 : 8;
            assertEquals(netScore, pv.value);
        }
        assertEquals(pvs.get(0).mover, startPosition.mover());
        assertEquals(pvs.get(0).enemy, startPosition.enemy());
    }

    public void testMove() {
        assertEquals("time and eval", "H8/-2.01/1.03", new MutableGame.Move(new MoveScore(0, -201), 1.03).toString());
        assertEquals("time", "H8//1.03", new MutableGame.Move(new MoveScore(0, 0), 1.03).toString());
        assertEquals("eval", "H8/-2.01", new MutableGame.Move(new MoveScore(0, -201), 0).toString());
        assertEquals("no time or eval", "H8", new MutableGame.Move(new MoveScore(0, 0), 0).toString());
    }
}
