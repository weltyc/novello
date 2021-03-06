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

package com.welty.novello.eval;

import com.welty.novello.core.BitBoardUtils;
import junit.framework.TestCase;

/**
 */
public class ColTermTest extends TestCase {
    private static final ColTerm left = new ColTerm(7);
    private static final ColTerm right = new ColTerm(0);

    public void testGetInstance() {
        final int expected = Base3.base2ToBase3(0x8C, 0x41);
        check(expected, 0x0000010100000001L, 0x0100000000000100L);
    }

    private void check(int expected, long mover, long enemy) {
        checkMini(expected, mover, enemy);
        final long moverExtra = 0xF00EF00EF00EF00EL;
        final long enemyExtra = 0x0EF00EF00EF00EF0L;
        checkMini(expected, mover | moverExtra, enemy|enemyExtra);
        checkMini(expected, mover | enemyExtra, enemy|moverExtra);
    }

    private static void checkMini(int expected, long mover, long enemy) {
        assertEquals(expected, right.instance(mover, enemy, 0, 0));
        final long rMover = BitBoardUtils.reflection(mover, 3);
        final long rEnemy = BitBoardUtils.reflection(enemy, 3);
        assertEquals(expected, left.instance(rMover, rEnemy, 0, 0));
    }
}
