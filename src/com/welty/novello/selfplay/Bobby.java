package com.welty.novello.selfplay;

import com.welty.novello.eval.CoefficientSet;
import com.welty.novello.eval.EvalStrategies;
import com.welty.novello.eval.StrategyBasedEval;

/**
 */

class Charlie extends EvalPlayer {
    public Charlie() {
        super(charlieEval);
    }

    private static final Eval charlieEval = new StrategyBasedEval(EvalStrategies.eval4, createCoeffSet());

    private static CoefficientSet createCoeffSet() {
        // coefficients  from eval4-D at 30 empties
        //    +24  No access to corner
        //    +2164  Mover access to corner
        //    -853  Enemy access to corner
        //    +171  Both access to corner
        //    +2177  Mover occupies corner
        //    -2138  Enemy occupies corner
        //    -1249  Mover x-square
        //    +1156  Enemy x-square

        final int[] coeffs = {
                24, 2164, -853, 171, 2177, -2138, -1249, 1156
        };

        int[][][] slices = new int[64][1][];
        for (int[][] slice : slices) {
            slice[0] = coeffs;
        }

        return new CoefficientSet(slices, "Charlie");
    }
}
