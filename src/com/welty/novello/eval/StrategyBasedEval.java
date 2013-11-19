package com.welty.novello.eval;

import com.welty.novello.selfplay.Eval;
import com.welty.novello.solver.BitBoardUtils;

/**
 * The evaluation function evaluates positions.
 * <p/>
 * It also connects the coefficient calculator to the features
 * by mapping feature orid &harr; coefficient calculator index.
 */
public class StrategyBasedEval implements Eval {

    final CoefficientSet coefficientSet;
    private final EvalStrategy evalStrategy = EvalStrategies.eval1;

    /**
     */
    public StrategyBasedEval() {
        coefficientSet = new CoefficientSet(evalStrategy);
    }

    @Override public int eval(long mover, long enemy) {
        return evalStrategy.eval(mover, enemy, coefficientSet);
    }

}
