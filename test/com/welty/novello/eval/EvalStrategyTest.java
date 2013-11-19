package com.welty.novello.eval;

import com.orbanova.common.misc.ArrayTestCase;
import com.welty.novello.solver.BitBoard;

/**
 */
public class EvalStrategyTest extends ArrayTestCase {
    public void testIndicesFromPosition() {
        final EvalStrategy strategy = EvalStrategies.eval1;

        // All 4 terms share the same feature
        assertEquals(1, strategy.nFeatures());

        // a sample position.
        // square 077 is mover, square 070 is enemy,
        // square 007 has mover access, square 000 has enemy access
        final long mover = 0x8001010101010100L;
        final long enemy = 0x0180808080808000L;
        System.out.println(new BitBoard(mover, enemy, true));
        final int[] expected = {2, 1, 5, 4};
        assertEquals(expected, strategy.coefficientIndices(mover, enemy));
    }

    public void testFeatureCompression() {

        final EvalStrategy strategy = new EvalStrategy(
                TermTest.term1,
                TermTest.term1,
                TermTest.term2
        );

        assertEquals(2, strategy.nFeatures());
        assertEquals(new int[]{3, 2}, strategy.nOridsByFeature());
        assertEquals(TermTest.feature1.nOrids() + TermTest.feature2.nOrids(), strategy.nCoefficientIndices());
        assertEquals(new int[] {2, 2, 3}, strategy.coefficientIndices(2, 0));
    }
}
