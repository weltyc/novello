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

        final int[] row0Coeffs = slice[9];
        eval += row0Coeffs[RowTerm.rowOrid(mover, enemy, 0)];
        eval += row0Coeffs[RowTerm.rowOrid(mover, enemy, 7)];
        eval += row0Coeffs[ColTerm.colOrid(mover, enemy, 0)];
        eval += row0Coeffs[ColTerm.colOrid(mover, enemy, 7)];

        final int[] row1Coeffs = slice[10];
        eval += row1Coeffs[RowTerm.rowOrid(mover, enemy, 1)];
        eval += row1Coeffs[RowTerm.rowOrid(mover, enemy, 6)];
        eval += row1Coeffs[ColTerm.colOrid(mover, enemy, 1)];
        eval += row1Coeffs[ColTerm.colOrid(mover, enemy, 6)];

        final int[] row2Coeffs = slice[11];
        eval += row2Coeffs[RowTerm.rowOrid(mover, enemy, 2)];
        eval += row2Coeffs[RowTerm.rowOrid(mover, enemy, 5)];
        eval += row2Coeffs[ColTerm.colOrid(mover, enemy, 2)];
        eval += row2Coeffs[ColTerm.colOrid(mover, enemy, 5)];

        final int[] row3Coeffs = slice[12];
        eval += row3Coeffs[RowTerm.rowOrid(mover, enemy, 3)];
        eval += row3Coeffs[RowTerm.rowOrid(mover, enemy, 4)];
        eval += row3Coeffs[ColTerm.colOrid(mover, enemy, 3)];
        eval += row3Coeffs[ColTerm.colOrid(mover, enemy, 4)];

        final int[] diagonal8Coeffs = slice[17];
        eval += diagonal8Coeffs[OridTable.orid8(DiagonalTerm.diagonalInstance(mover, enemy, 0x8040201008040201L, 56))];
        eval += diagonal8Coeffs[OridTable.orid8(DiagonalTerm.diagonalInstance(mover, enemy, 0x0102040810204080L, 56))];

        final int[] diagonal4Coeffs = slice[13];
        eval += diagonal4Coeffs[OridTable.orid4(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000000080402010L, 60))];
        eval += diagonal4Coeffs[OridTable.orid4(DiagonalTerm.diagonalInstance(mover, enemy, 0x0804020100000000L, 56))];
        eval += diagonal4Coeffs[OridTable.orid4(DiagonalTerm.diagonalInstance(mover, enemy, 0x1020408000000000L, 60))];
        eval += diagonal4Coeffs[OridTable.orid4(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000000001020408L, 56))];

        final int[] diagonal5Coeffs = slice[14];
        eval += diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000008040201008L, 59))];
        eval += diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x1008040201000000L, 56))];
        eval += diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x0810204080000000L, 59))];
        eval += diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000000102040810L, 56))];

        final int[] diagonal6Coeffs = slice[15];
        eval += diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000804020100804L, 58))];
        eval += diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x2010080402010000L, 56))];
        eval += diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x0408102040800000L, 58))];
        eval += diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000010204081020L, 56))];

        final int[] diagonal7Coeffs = slice[16];
        eval += diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x0080402010080402L, 57))];
        eval += diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x4020100804020100L, 56))];
        eval += diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x0204081020408000L, 57))];
        eval += diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x0001020408102040L, 56))];

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

    private static Term[] flatten(Object... others) {
        final ArrayList<Term> terms = new ArrayList<>();
        for (Object o : others) {
            if (o instanceof Term[]) {
                terms.addAll(Arrays.asList((Term[]) o));
            } else if (o instanceof Term) {
                terms.add((Term) o);
            } else {
                throw new IllegalStateException("oops. " + o.getClass());
            }
        }
        return terms.toArray(new Term[terms.size()]);
    }

    String generateCode() {
        StringBuilder sb = new StringBuilder();

        for (int iFeature = 9; iFeature<=12; iFeature++) {
            generateCodeForFeature(sb, iFeature);
        }

        // evaluating these features in reverse is faster, probably because it moves the long diagonals
        // to the front. The long diagonals share the length-8 orid table with the row and columns that immediately
        // precede it.
        for (int iFeature = 17; iFeature >= 13; iFeature--) {
            generateCodeForFeature(sb, iFeature);
        }

        return sb.toString();
    }

    private void generateCodeForFeature(StringBuilder sb, int iFeature) {
        final Feature feature = getFeature(iFeature);

        final ArrayList<Term> featureTerms = new ArrayList<>();

        for (Term term : terms) {
            if (term.getFeature().equals(feature)) {
                featureTerms.add(term);
            }
        }

        final String coeffName = feature.toString() + "Coeffs";
        sb.append(String.format("final int[] %s = slice[%d];\n", coeffName, iFeature));
        for (Term term : featureTerms) {
            appendCodeForTerm(sb, coeffName, term);
        }
        sb.append("\n");
    }

    private static void appendCodeForTerm(StringBuilder sb, String coeffs, Term term) {
        final String oridGen = term.oridGen();
        sb.append(String.format("eval += %s[%s];\n", coeffs, oridGen));
    }

    public static void main(String[] args) {
        System.out.println(new EvalStrategyB().generateCode());
    }
}
