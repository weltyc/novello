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

package com.welty.novello.ntest;

import com.welty.novello.core.MoveScore;
import junit.framework.TestCase;

public class NBoardPlayerTest extends TestCase {
    public void testParseMoveScore()  {
        assertEquals("Ntest format", new MoveScore("D3", -51), NBoardSyncEngine.parseMoveScore("=== D3/-0.51"));
        assertEquals("Edax format", new MoveScore("D6", 700), NBoardSyncEngine.parseMoveScore("=== d6 7.00 0.0"));
    }
}
