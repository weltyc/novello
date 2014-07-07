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

package com.welty.novello.core;

import com.welty.othello.gdk.OsClock;
import junit.framework.TestCase;

/**
 */
public class PositionTest extends TestCase {
    public void testUpdate() {
        final Position position = new Position(Board.START_BOARD, new OsClock(0.5), new OsClock(0.6));
        checkClocks(position, 0.5, 0.6);
        final Position position1 = position.playOrPass(new Move8x8("F5//0.01"));
        checkClocks(position1, 0.49, 0.6);
        final Position position2 = position1.playOrPass(new Move8x8("D6//1"));
        checkClocks(position2, 0.49, 119.6);
        assertEquals(1, position2.whiteClock.getITimeout());
    }

    private void checkClocks(Position position, double blackRemaining, double whiteRemaining) {
        assertEquals(blackRemaining, position.blackClock.tCurrent);
        assertEquals(whiteRemaining, position.whiteClock.tCurrent);
    }
}
