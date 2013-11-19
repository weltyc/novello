package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;
import com.welty.novello.solver.BitBoardUtils;

import java.util.ArrayList;

/**
 */
class EvalStrategy {
    private final Term[] terms;

    /**
     * Distinct features of terms, in order of their first appearance in terms
     */
    private final Feature[] features;


    /**
     * terms[i].getFeature() == features[iFeatures[i]]
     */
    private final int[] iFeatures;

    EvalStrategy(Term... terms) {
        this.terms = terms;
        iFeatures = new int[terms.length];

        final ArrayList<Feature> features = new ArrayList<>();
        for (int i = 0; i < terms.length; i++) {
            final Term term = terms[i];
            final Feature feature = term.getFeature();
            int iFeature = features.indexOf(feature);
            if (iFeature < 0) {
                iFeature = features.size();
                features.add(feature);
            }
            iFeatures[i] = iFeature;
        }
        this.features = features.toArray(new Feature[features.size()]);
    }

    String getFilename(int nEmpty) {
        return "coefficients/" + getClass().getSimpleName() + "_" + nEmpty + ".coeff";
    }

    /**
     * Calculate coefficient indices for the position.
     * <p/>
     * There is one coefficient index for each Term in this EvalStrategy.
     *
     * @param mover mover disks
     * @param enemy enemy disks
     * @return coefficient calculator indices for the position
     */
    public int[] coefficientIndices(long mover, long enemy) {
        final int[] coefficientIndices = new int[terms.length];

        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        final int[] coefficientIndexStarts = Vec.accumulate0(nOridsByFeature());
        for (int iTerm = 0; iTerm < terms.length; iTerm++) {
            final int orid = terms[iTerm].orid(mover, enemy, moverMoves, enemyMoves);
            final int iFeature = iFeatures[iTerm];
            final int coefficientIndex = orid + coefficientIndexStarts[iFeature];
            coefficientIndices[iTerm] = coefficientIndex;
        }
        return coefficientIndices;
    }

    public int eval(long mover, long enemy, CoefficientSet coefficientSet) {
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        final int[][] slice = coefficientSet.slice(BitBoardUtils.nEmpty(mover, enemy));

        int eval = 0;

        for (int iTerm = 0; iTerm < terms.length; iTerm++) {
            final Term term = terms[iTerm];
            final int iFeature = iFeatures[iTerm];

            final int orid = term.orid(mover, enemy, moverMoves, enemyMoves);

            eval += slice[iFeature][orid];
        }
        return eval;
    }

    /**
     * Calculate the total number of possible orids for all features.
     *
     * @return nOrids[iFeature] = the number of orids for that feature.
     */
    public int[] nOridsByFeature() {
        final int[] nOrids = new int[features.length];
        for (int i = 0; i < features.length; i++) {
            nOrids[i] = features[i].nOrids();
        }
        return nOrids;
    }

    /**
     * Print out the coefficients in human-readable form, with descriptions
     *
     * @param coefficients coefficients to print
     */
    public void dumpCoefficients(double[] coefficients) {
        Require.eq(coefficients.length, "# coefficients", nOridsByFeature());
        for (int i = 0; i < coefficients.length; i++) {
            System.out.format("%5.1f  %s%n", coefficients[i], terms[0].getFeature().oridDescription(i));
        }
    }

    public int nFeatures() {
        return features.length;
    }

    public Feature getFeature(int iFeature) {
        return features[iFeature];
    }

    public int nCoefficientIndices() {
        return Vec.sum(nOridsByFeature());
    }
}

/**
 * Utility class containing EvalStrategy instances
 */
class EvalStrategies {
    @SuppressWarnings("OctalInteger")
    static final EvalStrategy eval1 = new EvalStrategy(
            new CornerTerm(000),
            new CornerTerm(007),
            new CornerTerm(070),
            new CornerTerm(077)
    );
}