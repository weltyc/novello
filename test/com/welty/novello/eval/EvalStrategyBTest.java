package com.welty.novello.eval;

import com.orbanova.common.feed.Feeds;
import junit.framework.TestCase;

/**
 * Test EvalStrategyB diagonal code generation
 */
public class EvalStrategyBTest extends TestCase {
    public void testGenerateDiagonalEvalCode() throws Exception {
        final String expected = Feeds.ofLines(EvalStrategyBTest.class, "EvalStrategyBGeneratedCode.txt").join("\n") + "\n";
        assertEquals(expected, EvalStrategyB.generateDiagonalEvalCode());
    }
}
