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

import com.welty.othello.gdk.OsMoveListItem;
import junit.framework.TestCase;

public class MoveScoreTest extends TestCase {
    public void testToMli() throws Exception {
        final MoveScore a7 = new MoveScore("A7", 123);
        final OsMoveListItem mli = a7.toMli(0);
        assertEquals("A7/1.23", mli.toString());
    }
}
