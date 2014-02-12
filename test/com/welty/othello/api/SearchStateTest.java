package com.welty.othello.api;

import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsClock;
import junit.framework.TestCase;

public class SearchStateTest extends TestCase {
    public void testConstructor() {
        final COsGame game = new COsGame();
        game.Initialize("8", OsClock.DEFAULT, OsClock.DEFAULT);

        final SearchState searchState = new SearchState(game, 1, 0);
        assertEquals(0, searchState.getGame().getMoveList().size());
        assertEquals(0, searchState.getGame().nMoves());
    }
}
