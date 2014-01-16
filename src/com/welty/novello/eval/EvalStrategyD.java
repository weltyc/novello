package com.welty.novello.eval;

import com.orbanova.common.feed.Feeds;
import com.orbanova.common.feed.Mapper;
import com.welty.novello.core.BitBoardUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Evaluation with all rows, columns, and corner blocks
 */
@SuppressWarnings("OctalInteger")
class EvalStrategyD extends EvalStrategy {
    private static final int iDebugEval = 0;

    public EvalStrategyD() {
        super("d",
                flatten(CornerTerm2.terms,
                        Terms.moverDisks, Terms.enemyDisks, Terms.moverMobilities, Terms.enemyMobilities,
                        Terms.moverPotMobs, Terms.enemyPotMobs, Terms.moverPotMobs2, Terms.enemyPotMobs2,
                        Edge2XTerm.terms,
                        RowTerm.internalTerms,
                        ColTerm.internalTerms,
                        UldrTerm.terms,
                        UrdlTerm.terms,
                        CornerBlockTerm.terms,
                        Corner2x5Term.terms
                )
        );
    }

    @SuppressWarnings("ConstantConditions")
    @Override int eval(long mover, long enemy, long moverMoves, long enemyMoves, CoefficientSet coefficientSet) {
        assert moverMoves != 0;
        final short[][] slice = coefficientSet.slice(BitBoardUtils.nEmpty(mover, enemy));
        int eval = 0;

        final short[] corner2Coeffs = slice[0];
        eval += corner2Coeffs[CornerTerm2.orid(mover, enemy, moverMoves, enemyMoves, 0)]
                + corner2Coeffs[CornerTerm2.orid(mover, enemy, moverMoves, enemyMoves, 7)]
                + corner2Coeffs[CornerTerm2.orid(mover, enemy, moverMoves, enemyMoves, 56)]
                + corner2Coeffs[CornerTerm2.orid(mover, enemy, moverMoves, enemyMoves, 63)];
        if (iDebugEval > 1) {
            System.out.println("Corners done (features <= 0). eval = " + eval);
        }
        eval += slice[1][Long.bitCount(mover)];
        eval += slice[2][Long.bitCount(enemy)];
        eval += slice[3][Long.bitCount(moverMoves)];
        eval += slice[4][Long.bitCount(enemyMoves)];

        final long empty = ~(mover | enemy);
        eval += slice[5][Long.bitCount(BitBoardUtils.potMobs(mover, empty))];
        eval += slice[6][Long.bitCount(BitBoardUtils.potMobs(enemy, empty))];
        eval += slice[7][Long.bitCount(BitBoardUtils.potMobs2(mover, empty))];
        eval += slice[8][Long.bitCount(BitBoardUtils.potMobs2(enemy, empty))];

        if (iDebugEval > 1) {
            System.out.println("Corners, disks, and mobility done (features <= 8). eval = " + eval);
        }
        final short[] edge2XCoeffs = slice[9];
        eval += edge2XCoeffs[OridTable.orid10(Edge2XTerm.instance0(mover, enemy))]
                + edge2XCoeffs[OridTable.orid10(Edge2XTerm.instance1(mover, enemy))]
                + edge2XCoeffs[OridTable.orid10(Edge2XTerm.instance2(mover, enemy))]
                + edge2XCoeffs[OridTable.orid10(Edge2XTerm.instance3(mover, enemy))];

        final short[] row1Coeffs = slice[10];
        eval += row1Coeffs[RowTerm.rowOrid(mover, enemy, 1)]
                + row1Coeffs[RowTerm.rowOrid(mover, enemy, 6)]
                + row1Coeffs[ColTerm.colOrid(mover, enemy, 1)]
                + row1Coeffs[ColTerm.colOrid(mover, enemy, 6)];

        final short[] row2Coeffs = slice[11];
        eval += row2Coeffs[RowTerm.rowOrid(mover, enemy, 2)]
                + row2Coeffs[RowTerm.rowOrid(mover, enemy, 5)]
                + row2Coeffs[ColTerm.colOrid(mover, enemy, 2)]
                + row2Coeffs[ColTerm.colOrid(mover, enemy, 5)];

        final short[] row3Coeffs = slice[12];
        eval += row3Coeffs[RowTerm.rowOrid(mover, enemy, 3)]
                + row3Coeffs[RowTerm.rowOrid(mover, enemy, 4)]
                + row3Coeffs[ColTerm.colOrid(mover, enemy, 3)]
                + row3Coeffs[ColTerm.colOrid(mover, enemy, 4)];

        final short[] diagonal8Coeffs = slice[13];
        eval += diagonal8Coeffs[OridTable.orid8(DiagonalTerm.diagonalInstance(mover, enemy, 0x8040201008040201L, 56))]
                + diagonal8Coeffs[OridTable.orid8(DiagonalTerm.diagonalInstance(mover, enemy, 0x0102040810204080L, 56))];

        final short[] diagonal7Coeffs = slice[14];
        eval += diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x4020100804020100L, 56))]
                + diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x0080402010080402L, 57))]
                + diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x0001020408102040L, 56))]
                + diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x0204081020408000L, 57))];

        final short[] diagonal6Coeffs = slice[15];
        eval += diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x2010080402010000L, 56))]
                + diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000804020100804L, 58))]
                + diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000010204081020L, 56))]
                + diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x0408102040800000L, 58))];

        final short[] diagonal5Coeffs = slice[16];
        eval += diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x1008040201000000L, 56))]
                + diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000008040201008L, 59))]
                + diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000000102040810L, 56))]
                + diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x0810204080000000L, 59))];

        final short[] diagonal4Coeffs = slice[17];
        eval += diagonal4Coeffs[OridTable.orid4(DiagonalTerm.diagonalInstance(mover, enemy, 0x0804020100000000L, 56))]
                + diagonal4Coeffs[OridTable.orid4(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000000080402010L, 60))]
                + diagonal4Coeffs[OridTable.orid4(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000000001020408L, 56))]
                + diagonal4Coeffs[OridTable.orid4(DiagonalTerm.diagonalInstance(mover, enemy, 0x1020408000000000L, 60))];

        final short[] CornerBlockCoeffs = slice[18];
        eval += CornerBlockCoeffs[CornerBlockTerm.orid(mover, enemy, false, false)]
                + CornerBlockCoeffs[CornerBlockTerm.orid(mover, enemy, true, false)]
                + CornerBlockCoeffs[CornerBlockTerm.orid(mover, enemy, false, true)]
                + CornerBlockCoeffs[CornerBlockTerm.orid(mover, enemy, true, true)];

        final short[] Corner2x5Coeffs = slice[19];
        final int moverRow0 = BitBoardUtils.extractRow(mover, 0);
        final int enemyRow0 = BitBoardUtils.extractRow(enemy, 0);
        final int moverCol0 = BitBoardUtils.extractCol(mover, 0);
        final int enemyCol0 = BitBoardUtils.extractCol(enemy, 0);
        final int moverRow1 = BitBoardUtils.extractRow(mover, 1);
        final int enemyRow1 = BitBoardUtils.extractRow(enemy, 1);
        final int moverCol1 = BitBoardUtils.extractCol(mover, 1);
        final int enemyCol1 = BitBoardUtils.extractCol(enemy, 1);
        final int moverRow6 = BitBoardUtils.extractRow(mover, 6);
        final int enemyRow6 = BitBoardUtils.extractRow(enemy, 6);
        final int moverCol6 = BitBoardUtils.extractCol(mover, 6);
        final int enemyCol6 = BitBoardUtils.extractCol(enemy, 6);
        final int moverRow7 = BitBoardUtils.extractRow(mover, 7);
        final int enemyRow7 = BitBoardUtils.extractRow(enemy, 7);
        final int moverCol7 = BitBoardUtils.extractCol(mover, 7);
        final int enemyCol7 = BitBoardUtils.extractCol(enemy, 7);
        eval += Corner2x5Coeffs[Corner2x5Term.leftInstance(moverRow0, enemyRow0, moverRow1, enemyRow1)]
                + Corner2x5Coeffs[Corner2x5Term.rightInstance(moverRow0, enemyRow0, moverRow1, enemyRow1)]
                + Corner2x5Coeffs[Corner2x5Term.leftInstance(moverRow7, enemyRow7, moverRow6, enemyRow6)]
                + Corner2x5Coeffs[Corner2x5Term.rightInstance(moverRow7, enemyRow7, moverRow6, enemyRow6)]
                + Corner2x5Coeffs[Corner2x5Term.leftInstance(moverCol0, enemyCol0, moverCol1, enemyCol1)]
                + Corner2x5Coeffs[Corner2x5Term.rightInstance(moverCol0, enemyCol0, moverCol1, enemyCol1)]
                + Corner2x5Coeffs[Corner2x5Term.leftInstance(moverCol7, enemyCol7, moverCol6, enemyCol6)]
                + Corner2x5Coeffs[Corner2x5Term.rightInstance(moverCol7, enemyCol7, moverCol6, enemyCol6)];
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

        sb.append("assert moverMoves !=0;\n" +
                "final short[][] slice = coefficientSet.slice(BitBoardUtils.nEmpty(mover, enemy));\n" +
                "int eval = 0;\n");


        for (int iFeature = 0; iFeature < nFeatures(); iFeature++) {
            if (iFeature == 5) {
                if (!endsWithDoubleNewline(sb)) {
                    sb.append("\n");
                }
                sb.append("final long empty = ~(mover|enemy);\n");
            }
            generateCodeForFeature(sb, iFeature);
        }

        sb.append("return eval;\n");

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

        if (featureTerms.size() > 1) {
            if (!endsWithDoubleNewline(sb)) {
                sb.append('\n');
            }
            final String coeffName = (feature.toString() + "Coeffs").replace(" ", "");
            if (!coeffName.matches("[a-zA-Z][a-zA-Z0-9]*")) {
                throw new IllegalStateException("Coefficient name must be a valid java identifier");
            }
            sb.append(String.format("final short[] %s = slice[%d];\n", coeffName, iFeature));
            if (feature.getClass() == Corner2x5Feature.class) {
                for (int i : new int[]{0, 1, 6, 7}) {
                    sb.append("final int moverRow").append(i).append(" = BitBoardUtils.extractRow(mover, ").append(i).append(");\n");
                    sb.append("final int enemyRow").append(i).append(" = BitBoardUtils.extractRow(enemy, ").append(i).append(");\n");
                    sb.append("final int moverCol").append(i).append(" = BitBoardUtils.extractCol(mover, ").append(i).append(");\n");
                    sb.append("final int enemyCol").append(i).append(" = BitBoardUtils.extractCol(enemy, ").append(i).append(");\n");
                }
            }
            sb.append("eval += ");
            sb.append(
                    Feeds.of(featureTerms)
                            .map(new Mapper<Term, String>() {
                                @NotNull @Override public String y(Term term) {
                                    return coefficientCode(coeffName, term);
                                }
                            })
                            .join("\n     +  ")
            );
            sb.append(";\n");
        } else if (featureTerms.size() != 0) {
            sb.append(String.format("eval += slice[%d][%s];\n", iFeature, featureTerms.get(0).oridGen()));
        } else {
            throw new IllegalStateException("messed up");
        }
    }

    private static String coefficientCode(String coeffName, Term term) {
        return String.format("%s[%s]", coeffName, term.oridGen());
    }

    private boolean endsWithDoubleNewline(StringBuilder sb) {
        final int n = sb.length();
        return n > 1 && sb.charAt(n - 1) == '\n' && sb.charAt(n - 2) == '\n';
    }

    public static void main(String[] args) {
        System.out.println(new EvalStrategyD().generateCode());
    }
}
