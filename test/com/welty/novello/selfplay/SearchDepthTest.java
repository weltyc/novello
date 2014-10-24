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

import java.util.Arrays;
import java.util.List;

/**
 */
public class SearchDepthTest extends TestCase {
    public void testMidgameDepth() {
        final SearchDepth searchDepth = SearchDepth.maxDepth(60, 6, new MidgameSearcher.Options("NS"));
        assertEquals(6, searchDepth.depth);
        assertEquals(0, searchDepth.width);
    }

    public void testFeed() {
        final SearchDepth maxDepth = new SearchDepth(2, 0, 12);
        final List<SearchDepth> expected = Arrays.asList(new SearchDepth(1, 0, 12), new SearchDepth(2, 0, 12));
        assertEquals(expected, maxDepth.depthFeed().asList());
    }

    public void testFeedWithProbableSolve() {
        final SearchDepth maxDepth = new SearchDepth(2, 1, 2);
        final List<SearchDepth> expected = Arrays.asList(new SearchDepth(1, 0, 2), new SearchDepth(2, 0, 2), new SearchDepth(2, 1, 2));
        assertEquals(expected, maxDepth.depthFeed().asList());
    }
}
