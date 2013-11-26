package com.welty.novello.selfplay;

import com.welty.novello.solver.BitBoard;
import junit.framework.TestCase;

/**
 */

public class MutableGameTest extends TestCase {

    private static final BitBoard START_POSITION = new BitBoard(0x0000000810000000L, 0x0000001008000000L, true);

    public void testUpdates() {
        final BitBoard startPosition = START_POSITION;
        final MutableGame game = new MutableGame(startPosition);
        assertEquals(START_POSITION, game.getStartPosition());
        assertEquals(START_POSITION, game.getLastPosition());
        assertTrue(game.toGgf().contains("BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]"));
        assertTrue(game.toGgf().startsWith("(;GM[Othello]"));
        assertTrue(game.toGgf().endsWith(";)"));

        game.play("F5");
        final BitBoard nextPosition = new BitBoard(0x000000081C000000L, 0x0000001000000000L, false);
        assertEquals(START_POSITION, game.getStartPosition());
        assertEquals(nextPosition, game.getLastPosition());
        assertTrue(game.toGgf().contains("BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]"));
        assertTrue(game.toGgf().contains("B[F5]"));

        game.play("D6//1.03");
        System.out.println(game.toGgf());
        assertTrue(game.toGgf().contains("W[D6//1.03]"));
    }
}
