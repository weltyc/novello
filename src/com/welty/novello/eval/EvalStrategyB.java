package com.welty.novello.eval;

import com.welty.novello.core.BitBoardUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
* Evaluation with all rows, columns, and corner blocks
*/
@SuppressWarnings("OctalInteger")
class EvalStrategyB extends EvalStrategy {
    private final CornerTerm2[] cornerTerms;
    private final RowTerm[] rowTerms;
    private final ColTerm[] colTerms;
    private final UldrTerm[] uldrTerms;
    private final UrdlTerm[] urdlTerms;
    private final CornerBlockTerm[] cornerBlockTerms;

    public EvalStrategyB() {
        this(new CornerTerm2[]{new CornerTerm2(000), new CornerTerm2(007), new CornerTerm2(070), new CornerTerm2(077)},
                new RowTerm[]{new RowTerm(0), new RowTerm(1), new RowTerm(2), new RowTerm(3), new RowTerm(4), new RowTerm(5), new RowTerm(6), new RowTerm(7)},
                new ColTerm[]{new ColTerm(0), new ColTerm(1), new ColTerm(2), new ColTerm(3), new ColTerm(4), new ColTerm(5), new ColTerm(6), new ColTerm(7)},
                new UldrTerm[]{new UldrTerm(-4), new UldrTerm(-3), new UldrTerm(-2), new UldrTerm(-1), new UldrTerm(-0), new UldrTerm(1), new UldrTerm(2), new UldrTerm(3), new UldrTerm(4)},
                new UrdlTerm[]{new UrdlTerm(-4), new UrdlTerm(-3), new UrdlTerm(-2), new UrdlTerm(-1), new UrdlTerm(-0), new UrdlTerm(1), new UrdlTerm(2), new UrdlTerm(3), new UrdlTerm(4)},
                new CornerBlockTerm[]{new CornerBlockTerm(false, false), new CornerBlockTerm(false, true), new CornerBlockTerm(true, false), new CornerBlockTerm(true, true)}
        );
    }

    public EvalStrategyB(CornerTerm2[] cornerTerms, RowTerm[] rowTerms, ColTerm[] colTerms, UldrTerm[] uldrTerms
            , UrdlTerm[] urdlTerms, CornerBlockTerm[] cornerBlockTerms) {
        super("b",
                flatten(cornerTerms,
                        Terms.moverDisks, Terms.enemyDisks, Terms.moverMobilities, Terms.enemyMobilities,
                        Terms.moverPotMobs, Terms.enemyPotMobs, Terms.moverPotMobs2, Terms.enemyPotMobs2,
                        rowTerms,
                        colTerms,
                        uldrTerms,
                        urdlTerms,
                        cornerBlockTerms
                )
        );
        this.cornerTerms = cornerTerms;
        this.rowTerms = rowTerms;
        this.colTerms = colTerms;
        this.uldrTerms = uldrTerms;
        this.urdlTerms = urdlTerms;
        this.cornerBlockTerms = cornerBlockTerms;
    }

    @Override
    int eval(long mover, long enemy, long moverMoves, long enemyMoves, CoefficientSet coefficientSet) {
        assert moverMoves != 0;

        final int[][] slice = coefficientSet.slice(BitBoardUtils.nEmpty(mover, enemy));

        int eval = 0;

        // evaluate corner features separately to see if specialization helps the timing
        final Feature cornerFeature = cornerTerms[0].getFeature();
        final int[] cornerFeatureCoeffs = slice[0];
        for (final CornerTerm2 term : cornerTerms) {
            final int instance = term.instance(mover, enemy, moverMoves, enemyMoves);
            final int orid = cornerFeature.orid(instance);
            final int coeff = cornerFeatureCoeffs[orid];
            eval += coeff;
        }

        eval += slice[1][Terms.moverDisks.instance(mover, enemy, moverMoves, enemyMoves)];
        eval += slice[2][Terms.enemyDisks.instance(mover, enemy, moverMoves, enemyMoves)];
        eval += slice[3][Terms.moverMobilities.instance(mover, enemy, moverMoves, enemyMoves)];
        eval += slice[4][Terms.enemyMobilities.instance(mover, enemy, moverMoves, enemyMoves)];
        eval += slice[5][Terms.moverPotMobs.instance(mover, enemy, moverMoves, enemyMoves)];
        eval += slice[6][Terms.enemyPotMobs.instance(mover, enemy, moverMoves, enemyMoves)];
        eval += slice[7][Terms.moverPotMobs2.instance(mover, enemy, moverMoves, enemyMoves)];
        eval += slice[8][Terms.enemyPotMobs2.instance(mover, enemy, moverMoves, enemyMoves)];

        final Feature row0Feature = rowTerms[0].getFeature();
        final int[] row0FeatureCoeffs = slice[9];
        eval += row0FeatureCoeffs[row0Feature.orid(rowTerms[0].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += row0FeatureCoeffs[row0Feature.orid(rowTerms[7].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += row0FeatureCoeffs[colOrid(mover, enemy, 0)];
        eval += row0FeatureCoeffs[colOrid(mover, enemy, 7)];

        final Feature row1Feature = rowTerms[1].getFeature();
        final int[] row1FeatureCoeffs = slice[10];
        eval += row1FeatureCoeffs[row1Feature.orid(rowTerms[1].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += row1FeatureCoeffs[row1Feature.orid(rowTerms[6].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += row1FeatureCoeffs[colOrid(mover, enemy, 1)];
        eval += row1FeatureCoeffs[colOrid(mover, enemy, 6)];

        final Feature row2Feature = rowTerms[2].getFeature();
        final int[] row2FeatureCoeffs = slice[11];
        eval += row2FeatureCoeffs[row2Feature.orid(rowTerms[2].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += row2FeatureCoeffs[row2Feature.orid(rowTerms[5].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += row2FeatureCoeffs[colOrid(mover, enemy, 2)];
        eval += row2FeatureCoeffs[colOrid(mover, enemy, 5)];

        final Feature row3Feature = rowTerms[3].getFeature();
        final int[] row3FeatureCoeffs = slice[12];
        eval += row3FeatureCoeffs[row3Feature.orid(rowTerms[3].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += row3FeatureCoeffs[row3Feature.orid(rowTerms[4].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += row3FeatureCoeffs[colOrid(mover, enemy, 3)];
        eval += row3FeatureCoeffs[colOrid(mover, enemy, 4)];

        final Feature uldr0Feature = uldrTerms[4].getFeature();
        final int[] uldr0FeatureCoeffs = slice[17];
        eval += uldr0FeatureCoeffs[uldr0Feature.orid(uldrTerms[4].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr0FeatureCoeffs[uldr0Feature.orid(urdlTerms[4].instance(mover, enemy, moverMoves, enemyMoves))];

        final Feature uldr1Feature = uldrTerms[5].getFeature();
        final int[] uldr1FeatureCoeffs = slice[16];
        eval += uldr1FeatureCoeffs[uldr1Feature.orid(uldrTerms[3].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr1FeatureCoeffs[uldr1Feature.orid(uldrTerms[5].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr1FeatureCoeffs[uldr1Feature.orid(urdlTerms[3].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr1FeatureCoeffs[uldr1Feature.orid(urdlTerms[5].instance(mover, enemy, moverMoves, enemyMoves))];

        final Feature uldr2Feature = uldrTerms[6].getFeature();
        final int[] uldr2FeatureCoeffs = slice[15];
        eval += uldr2FeatureCoeffs[uldr2Feature.orid(uldrTerms[2].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr2FeatureCoeffs[uldr2Feature.orid(uldrTerms[6].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr2FeatureCoeffs[uldr2Feature.orid(urdlTerms[2].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr2FeatureCoeffs[uldr2Feature.orid(urdlTerms[6].instance(mover, enemy, moverMoves, enemyMoves))];

        final Feature uldr3Feature = uldrTerms[7].getFeature();
        final int[] uldr3FeatureCoeffs = slice[14];
        eval += uldr3FeatureCoeffs[uldr3Feature.orid(uldrTerms[1].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr3FeatureCoeffs[uldr3Feature.orid(uldrTerms[7].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr3FeatureCoeffs[uldr3Feature.orid(urdlTerms[1].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr3FeatureCoeffs[uldr3Feature.orid(urdlTerms[7].instance(mover, enemy, moverMoves, enemyMoves))];

        final Feature uldr4Feature = uldrTerms[8].getFeature();
        final int[] uldr4FeatureCoeffs = slice[13];
        eval += uldr4FeatureCoeffs[uldr4Feature.orid(uldrTerms[0].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr4FeatureCoeffs[uldr4Feature.orid(uldrTerms[8].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr4FeatureCoeffs[uldr4Feature.orid(urdlTerms[0].instance(mover, enemy, moverMoves, enemyMoves))];
        eval += uldr4FeatureCoeffs[uldr4Feature.orid(urdlTerms[8].instance(mover, enemy, moverMoves, enemyMoves))];

        final Feature cornerBlockFeature = cornerBlockTerms[0].getFeature();
        final int[] cornerBlockFeatureCoeffs = slice[18];
        for (final CornerBlockTerm term : cornerBlockTerms) {
            final int instance = term.instance(mover, enemy, moverMoves, enemyMoves);
            final int orid = cornerBlockFeature.orid(instance);
            final int coeff = cornerBlockFeatureCoeffs[orid];
            eval += coeff;
        }
        return eval;
    }

    private int colOrid(long mover, long enemy, int col) {
        return OridTable.orid8(ColTerm.colInstance(mover, enemy, col));
    }

    private static Term[] flatten(Object... others) {
        final ArrayList<Term> terms = new ArrayList<>();
        for (Object o : others) {
            if (o instanceof Term[]) {
                terms.addAll(Arrays.asList((Term[]) o));
            }
            else if (o instanceof Term) {
                terms.add((Term)o);
            }
            else {
                throw new IllegalStateException("oops. " + o.getClass());
            }
        }
        return terms.toArray(new Term[terms.size()]);
    }
}