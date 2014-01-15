package com.welty.novello.eval;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Me;
import com.welty.ntestj.CEvaluatorJ;
import junit.framework.TestCase;

public class EvalStrategyJTest extends TestCase {
    static final char[] NTEST_EDGE_CONFIGS = {1093, 3362, 3305, 1822};
    static final int[] NOVELLO_EDGE_INSTANCES = {4374, 82, 34, 5103};
    static final Me me = Me.early;

    public void testEval() throws Exception {
        final CEvaluatorJ ntestJ = CEvaluatorJ.getInstance();
        final EvalStrategy strategyJ = EvalStrategies.strategy("j");
        final CoefficientEval novelloJ = new CoefficientEval(strategyJ, CEvaluatorJ.getInstance().getNovelloCoeffs());
        final int ntestScore = ntestJ.eval(me.mover, me.enemy);
        final int novelloScore = novelloJ.eval(me.mover, me.enemy);
        assertEquals(ntestScore, novelloScore);
    }

    public void testBase3Printing() {
        final int config = BitBoardUtils.rowInstance(me.mover, me.enemy, 0);
        assertEquals(2 * 3 * 3 * 3 * 81, config);
        assertEquals("O.......", Base3.description(config, 8));
    }

    public void testNovelloInstanceFromNtestConfig() {
        for (int i = 0; i < 4; i++) {
            final int novelloInstance = NOVELLO_EDGE_INSTANCES[i];
            final char ntestConfig = NTEST_EDGE_CONFIGS[i];
            assertEquals(novelloInstance, CEvaluatorJ.novelloInstanceFromNtestConfig(ntestConfig, 0));
        }

        final int[] novello2x5 = {14096, 39366, 40851, 15148};
        final char[] ntest2x5 = {29258, 29443, 36001, 49419};

        // C2x5J must be reordered, ntest has second row in low trits; novello has second row in high trits.
        for (int i=0; i<4; i++) {
            final int novelloInstance = novello2x5[i];
            final char ntestConfig = ntest2x5[i];
            assertEquals(novelloInstance, CEvaluatorJ.novelloInstanceFromNtestConfig(ntestConfig, CEvaluatorJ.C2x5J));
        }
    }
}
