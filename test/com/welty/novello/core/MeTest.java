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

import junit.framework.TestCase;

public class MeTest extends TestCase {
    public void testSubPositions() throws Exception {
        final int nExpected = Long.bitCount(Me.early.calcMoves());
        assertEquals(nExpected, Me.early.subPositions().size());
    }

    public void testMinimalReflection() {
        testMinimalReflection(Me.early);
        testMinimalReflection(Me.late);

    }

    private void testMinimalReflection(Me me) {
        final Me minimal = me.minimalReflection();
        boolean matches = minimal.equals(me);

        for (int r=1; r<8; r++) {
            final long rMover = BitBoardUtils.reflection(me.mover, r);
            final long rEnemy = BitBoardUtils.reflection(me.enemy, r);
            final Me reflected = new Me(rMover, rEnemy);
            matches |= minimal.equals(reflected);
            assertEquals(minimal, reflected.minimalReflection());
        }

        assertTrue("One reflection must be the minimal reflection", matches);
    }
}
