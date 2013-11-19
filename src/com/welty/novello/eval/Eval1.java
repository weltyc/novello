package com.welty.novello.eval;

import com.welty.novello.selfplay.Eval;
import com.welty.novello.solver.BitBoardUtils;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * The evaluation function evaluates positions.
 * <p/>
 * It also connects the coefficient calculator to the features
 * by mapping feature orid &harr; coefficient calculator index.
 */
public class Eval1 implements Eval {

    final CoefficientSet coefficientSet;
    private final EvalStrategy evalStrategy = new EvalStrategy();

    /**
     */
    public Eval1() {
        coefficientSet = new CoefficientSet(evalStrategy);
    }

    @Override public int eval(long mover, long enemy) {
        final int nEmpty = BitBoardUtils.nEmpty(mover, enemy);
        final int[] coeffs = coefficientSet.coeffs[nEmpty];
        final int[] indices = evalStrategy.oridsFromPosition(mover, enemy);
        int result = 0;
        for (int index : indices) {
            result += coeffs[index];
        }
        return result;
    }

    static class CoefficientSet {
        final int[][] coeffs = new int[64][];

        public CoefficientSet(EvalStrategy strategy) {
            for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
                final int n = strategy.nOrids();
                coeffs[nEmpty] = new int[n];
                final String filename = strategy.getFilename(nEmpty);
                try (DataInputStream in = new DataInputStream(new FileInputStream(filename))) {
                    for (int i = 0; i < n; i++) {
                        coeffs[nEmpty][i] = in.readInt();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
