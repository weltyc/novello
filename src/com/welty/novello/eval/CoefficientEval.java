package com.welty.novello.eval;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Position;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * The evaluation function evaluates positions.
 * <p/>
 * It also connects the coefficient calculator to the features
 * by mapping feature orid &harr; coefficient calculator index.
 */
public class CoefficientEval extends Eval {
    private final CoefficientSet coefficientSet;
    private final EvalStrategy evalStrategy;

    private static final boolean debug = false;


    /**
     * The Multi-probcut calcs.
     * <p/>
     * It is possible for this to be null (for example, when calculating MPC statistics for the first time).
     * This will lead to a NullPointerException if any Search tries to use it.
     */
    public @Nullable Mpc mpc;

    public CoefficientEval(EvalStrategy evalStrategy, String coeffSetName) {
        this(evalStrategy, new CoefficientSet(evalStrategy, coeffSetName));
    }

    /**
     * Create an eval using a strategy and coefficients that don't vary by nEmpty
     */
    CoefficientEval(EvalStrategy evalStrategy, short[][] coefficients) {
        this(evalStrategy, new CoefficientSet(coefficients, evalStrategy + "FixedCoeffs"));
    }

    private CoefficientEval(EvalStrategy evalStrategy, CoefficientSet coeffSet) {
        this.evalStrategy = evalStrategy;
        coefficientSet = coeffSet;
        mpc = createMpc();
    }

    private Mpc createMpc() {
        try {
            return new Mpc(getMpcSliceData());
        } catch (IOException e) {
            return new Mpc();
        }
    }

    ArrayList<int[]>[] getMpcSliceData() throws IOException {
        final Path path = getCoeffDir().resolve("mpc.txt");
        return Mpc.readSliceData(path);

    }

    /**
     * Evaluate a position.
     * <p/>
     * Passes if necessary; returns terminal value if neither player can move.
     *
     * @param mover mover disks
     * @param enemy enemy disks
     * @return position evaluation
     */
    @Override public int eval(long mover, long enemy) {
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        final int moveScore;
        if (moverMoves != 0) {
            moveScore = eval(mover, enemy, moverMoves, enemyMoves);
        } else if (enemyMoves != 0) {
            moveScore = -eval(enemy, mover, enemyMoves, moverMoves);
        } else {
            moveScore = CoefficientCalculator.DISK_VALUE * BitBoardUtils.terminalScore(mover, enemy);
        }
        return moveScore;
    }

    /**
     * Evaluate a position.
     * <p/>
     * Precondition: mover has a legal move.
     *
     * @param mover      mover disks
     * @param enemy      enemy disks
     * @param moverMoves legal moves for mover
     * @param enemyMoves legal moves for enemy
     * @return board evaluation, from mover's point of view
     */
    private int eval(long mover, long enemy, long moverMoves, long enemyMoves) {
        if (debug) {
            System.out.println("....................");
            System.out.println(Position.ofMover(mover, enemy, false));
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
    void explain(long mover, long enemy, long moverMoves, long enemyMoves) {
        evalStrategy.explain(mover, enemy, moverMoves, enemyMoves, coefficientSet);
    }

    @Override public String toString() {
        return evalStrategy + "" + coefficientSet;
    }

    /**
     * @return the directory where coefficients for this eval are stored
     */
    public Path getCoeffDir() {
        return evalStrategy.coeffDir(coefficientSet.name);
    }
}
