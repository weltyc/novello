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

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 */
public class SearchDepthTest extends TestCase {
    public void testMidgameDepth() {
        final SearchDepth last = lastDepth(60, 6);
        assertEquals(6, last.depth);
        assertEquals(0, last.width);
    }

    private static SearchDepth lastDepth(int nEmpty, int maxMidgameDepth) {
        final List<SearchDepth> searchDepths = SearchDepths.calcSearchDepths(nEmpty, maxMidgameDepth);
        return searchDepths.get(searchDepths.size() - 1);
    }

    public void testFeed() {
        final List<SearchDepth> expected = Arrays.asList(new SearchDepth(1, 0, 12), new SearchDepth(2, 0, 12));
        assertEquals(expected, SearchDepths.calcSearchDepths(17, 2));
    }

    public void test1PlySearchDepth() {
        // at 1 ply we should just do a full-width search.
        final List<SearchDepth> expected = Arrays.asList(new SearchDepth(1, Integer.MAX_VALUE, 1));
        assertEquals(expected, SearchDepths.calcSearchDepths(6, 1));
        assertEquals(expected, SearchDepths.calcSearchDepths(6, 10));
    }

    public void testFeedWithProbableSolve() {
        // probable solves wider than width=0 are ignored due to the shallowness of the search.
        final List<SearchDepth> expected = Arrays.asList(new SearchDepth(1, 0, 2), new SearchDepth(2, 0, 2), new SearchDepth(2, Integer.MAX_VALUE, 2));
        assertEquals(expected, SearchDepths.calcSearchDepths(7, 2));
    }

    public void testFeedWithProbableSolve2() {
        // probable solves with multiple widths are created.
        final List<SearchDepth> expected = Arrays.asList(
                new SearchDepth(15, 0, 16),
                new SearchDepth(16, 0, 16),
                new SearchDepth(16, 1, 16),
                new SearchDepth(16, 2, 16),
                new SearchDepth(16, 3, 16),
                new SearchDepth(16, 4, 16),
                new SearchDepth(16, 5, 16),
                new SearchDepth(16, 6, 16),
                new SearchDepth(16, 7, 16)
                );
        final List<SearchDepth> actual = SearchDepths.calcSearchDepths(21, 18);
        assertEquals(expected, actual.subList(14, actual.size()));
    }

    public void testMaxMpcWidth() {
        for (int midgame = 10; midgame <= 60; midgame += 10) {
            System.out.println("\nMidgame depth : " + midgame);
            System.out.println(SearchDepths.maxes(midgame));
            System.out.println();
        }
    }
}
