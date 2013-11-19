package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.welty.novello.solver.BitBoardUtils;

import java.util.ArrayList;

/**
 */
class EvalStrategy {
    @SuppressWarnings("OctalInteger")
    static final EvalStrategy eval1 = new EvalStrategy(
            new CornerTerm(000),
            new CornerTerm(007),
            new CornerTerm(070),
            new CornerTerm(077)
    );

    private final Term[] terms;

    private final Feature[] features;

    EvalStrategy(Term... terms) {
        this.terms = terms;

        final ArrayList<Feature> features = new ArrayList<>();
        for (Term term : terms) {
            final Feature feature = term.getFeature();
            if (!features.contains(feature)) {
                features.add(feature);
            }
        }
        this.features = features.toArray(new Feature[features.size()]);
    }

    String getFilename(int nEmpty) {
        return "coefficients/" + getClass().getSimpleName() + "_" + nEmpty + ".coeff";
    }

    /**
     * @param mover mover disks
     * @param enemy enemy disks
     * @return coefficient calculator indices for the position
     */
    public int[] oridsFromPosition(long mover, long enemy) {
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        final int[] result = new int[terms.length];
        for (int i = 0; i < terms.length; i++) {
            result[i] = terms[i].orid(mover, enemy, moverMoves, enemyMoves);
        }
        return result;
    }

    /**
     * Calculate the total number of possible orids for all features.
     */
    public int nOrids() {
        int nOrids = 0;
        for (Feature feature : features) {
            nOrids += feature.nOrids();
        }
        return nOrids;
    }

    /**
     * @return the total number of possible instances for all features
     */
    public int nInstances() {
        int nInstances = 0;
        for (Feature feature : features) {
            nInstances += feature.nInstances();
        }
        return nInstances;
    }

    /**
     * Print out the coefficients in human-readable form, with descriptions
     * @param coefficients coefficients to print
     */
    public void dumpCoefficients(double[] coefficients) {
        Require.eq(coefficients.length, "# coefficients", nOrids());
        for (int i = 0; i < coefficients.length; i++) {
            System.out.format("%5.1f  %s%n", coefficients[i], terms[0].getFeature().oridDescription(i));
        }
    }
}
