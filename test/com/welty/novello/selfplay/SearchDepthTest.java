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

package com.welty.novello.selfplay;

import com.welty.novello.solver.MidgameSearcher;
import junit.framework.TestCase;

/**
 */
public class SearchDepthTest extends TestCase {
    public void testMidgameDepth() {
        final SearchDepth searchDepth = SearchDepth.maxDepth(60, 6, new MidgameSearcher.Options("NS"));
        assertEquals(6, searchDepth.depth);
    }
}
