package com.welty.othello.api;

import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsClock;
import junit.framework.TestCase;

public class NBoardStateTest extends TestCase {
    public void testConstructor() {
        final COsGame game = new COsGame();
        game.Initialize("8", OsClock.DEFAULT, OsClock.DEFAULT);

        final NBoardState NBoardState = new NBoardState(game, 1, 0);
        assertEquals(0, NBoardState.getGame().getMoveList().size());
        assertEquals(0, NBoardState.getGame().nMoves());
    }
}
