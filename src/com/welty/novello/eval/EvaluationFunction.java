package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
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
public class EvaluationFunction implements Eval {
    final Term[] terms = {
            new CornerTerm(000),
            new CornerTerm(007),
            new CornerTerm(070),
            new CornerTerm(077)
    };

    final Coefficients coefficients;

    /**
     * @param loadCoefficients if true, read coefficients from file. If false, coefficients are not loaded.
     */
    public EvaluationFunction(boolean loadCoefficients) {
        if (loadCoefficients) {
            coefficients = new Coefficients(this);
        } else {
            coefficients = null;
        }
    }

    String getFilename(int nEmpty) {
        return "coefficients/" + getClass().getSimpleName() + "_" + nEmpty + ".coeff";
    }

    /**
     * @param mover mover disks
     * @param enemy enemy disks
     * @return coefficient calculator indices for the position
     */
    public int[] indicesFromPosition(long mover, long enemy) {
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        final int[] result = new int[4];
        for (int i = 0; i < 4; i++) {
            result[i] = terms[i].orid(mover, enemy, moverMoves, enemyMoves);
        }
        return result;
    }

    /**
     * Calculate the number of possible indices returned from indicesFromPosition.
     * <p/>
     * This is one more than the highest index value returned from indicesFromPosition.
     *
     * @return number of possible indices returned from indicesFromPosition
     */
    public int nIndices() {
        return terms[0].getFeature().nOrids();
    }

    /**
     * Print out the coefficients in human-readable form, with descriptions
     * @param coefficients coefficients to print
     */
    public void dumpCoefficients(double[] coefficients) {
        Require.eq(coefficients.length, "# coefficients", nIndices());
        for (int i = 0; i < coefficients.length; i++) {
            System.out.format("%5.1f  %s%n", coefficients[i], terms[0].getFeature().oridDescription(i));
        }
    }

    @Override public int eval(long mover, long enemy) {
        final int nEmpty = BitBoardUtils.nEmpty(mover, enemy);
        final int[] coeffs = coefficients.coeffs[nEmpty];
        final int[] indices = indicesFromPosition(mover, enemy);
        int result = 0;
        for (int index : indices) {
            result += coeffs[index];
        }
        return result;
    }

    static class Coefficients {
        final int[][] coeffs = new int[64][];

        public Coefficients(EvaluationFunction evaluationFunction) {
            for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
                final int n = evaluationFunction.nIndices();
                coeffs[nEmpty] = new int[n];
                final String filename = evaluationFunction.getFilename(nEmpty);
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
