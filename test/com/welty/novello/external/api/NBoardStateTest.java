/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.external.api;

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
