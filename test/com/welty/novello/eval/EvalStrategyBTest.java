package com.welty.novello.eval;

import com.orbanova.common.feed.Feeds;
import junit.framework.TestCase;

/**
 * Test EvalStrategyB diagonal code generation
 */
public class EvalStrategyBTest extends TestCase {
    public void testGenerateEvalCode() throws Exception {
        final String expected = Feeds.ofLines(EvalStrategyBTest.class, "EvalStrategyBGeneratedCode.txt").join("\n").trim();
        assertEquals(expected, new EvalStrategyB().generateCode().trim());
    }
}
