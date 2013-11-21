package com.welty.novello.eval;

import com.welty.novello.selfplay.Eval;
import com.welty.novello.solver.BitBoard;

/**
 * The evaluation function evaluates positions.
 * <p/>
 * It also connects the coefficient calculator to the features
 * by mapping feature orid &harr; coefficient calculator index.
 */
public class StrategyBasedEval implements Eval {
    final CoefficientSet coefficientSet;
    private final EvalStrategy evalStrategy;

    private static final boolean debug = false;

    /**
     */
    public StrategyBasedEval(EvalStrategy evalStrategy, String coeffSetName) {
        this.evalStrategy = evalStrategy;
        coefficientSet = new CoefficientSet(evalStrategy, coeffSetName);
    }

    @Override public int eval(long mover, long enemy, long moverMoves, long enemyMoves) {
        if (debug) {
            System.out.println("....................");
            System.out.println(BitBoard.ofMover(mover, enemy, false));
        }
        final int eval = evalStrategy.eval(mover, enemy, moverMoves, enemyMoves, coefficientSet);
        if (debug) {
            System.out.println("Eval = " + eval);
            System.out.println();
        }
        return eval;
    }

    @Override public String toString() {
        return evalStrategy+"/"+coefficientSet;
    }
}
