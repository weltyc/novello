package com.welty.novello.eval;

import com.welty.novello.solver.BitBoard;

/**
 * The evaluation function evaluates positions.
 * <p/>
 * It also connects the coefficient calculator to the features
 * by mapping feature orid &harr; coefficient calculator index.
 */
public class Eval {
    final CoefficientSet coefficientSet;
    private final EvalStrategy evalStrategy;

    private static final boolean debug = false;

    /**
     */
    public Eval(EvalStrategy evalStrategy, String coeffSetName) {
        this(evalStrategy, new CoefficientSet(evalStrategy, coeffSetName));
    }

    public Eval(EvalStrategy evalStrategy, CoefficientSet coeffSet) {
        this.evalStrategy = evalStrategy;
        coefficientSet = coeffSet;
    }

    /**
         * Evaluate a position.
         * <p/>
         * It is guaranteed that the mover has a legal move.
         *
         * @param mover      mover disks
         * @param enemy      enemy disks
         * @param moverMoves legal moves for mover
         * @param enemyMoves legal moves for enemy
         * @return board evaluation, from mover's point of view
         */
    public int eval(long mover, long enemy, long moverMoves, long enemyMoves) {
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

    /**
         * Explain its evaluation of a position
         *
         * @param mover      mover disks
         * @param enemy      enemy disks
         * @param moverMoves legal moves for mover
         * @param enemyMoves legal moves for enemy
         */
    public void explain(long mover, long enemy, long moverMoves, long enemyMoves) {
        evalStrategy.explain(mover, enemy, moverMoves, enemyMoves, coefficientSet);
    }

    @Override public String toString() {
        return evalStrategy+""+coefficientSet;
    }
}
