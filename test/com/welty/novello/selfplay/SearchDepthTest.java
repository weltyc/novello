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
