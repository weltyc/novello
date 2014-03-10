package com.welty.novello.eval;

import com.orbanova.common.feed.Feeds;
import com.orbanova.common.feed.Mapper;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Board;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Evaluation designed to match Ntest's CEvaluatorJ
 */
@SuppressWarnings("OctalInteger") public class EvalStrategyF extends EvalStrategy {
    private static final Object[] allTerms = {
            RowTerm.internalTerms,
            ColTerm.internalTerms,
            // ntest diagonal coefficients skip the length-4 diagonal; it's in the corner evaluator.
            new UldrTerm[]{
                    new UldrTerm(0), new UldrTerm(1), new UldrTerm(-1), new UldrTerm(2), new UldrTerm(-2), new UldrTerm(3), new UldrTerm(-3)
            },
            new UrdlTerm[]{
                    new UrdlTerm(0), new UrdlTerm(1), new UrdlTerm(-1), new UrdlTerm(2), new UrdlTerm(-2), new UrdlTerm(3), new UrdlTerm(-3)
            },
            CornerTriangleTerm.terms,
            Corner2x5Term.terms,
            Edge2XTerm.terms,
            Terms.moverMobilities64,
            Terms.enemyMobilities64,
            Terms.enemyLinearPotMobs,
            Terms.moverLinearPotMobs,
            Terms.parity

    };

    public EvalStrategyF() {
        super("f", flatten(allTerms));
    }

    private static final int iDebugEval = 0;

    @Override int eval(long mover, long enemy, long moverMoves, long enemyMoves, CoefficientSet coefficientSet) {
        assert moverMoves != 0;
        final int nEmpty = BitBoardUtils.nEmpty(mover, enemy);
        final short[][] slice = coefficientSet.slice(nEmpty);
        int eval = 0;

        if (iDebugEval > 1) {
            System.out.println("----------------------------");
            System.out.println(new Board(mover, enemy, true));
            System.out.format("mover = 0x%x, enemy = 0x%x\n", mover, enemy);
        }

        final short[] row1Coeffs = slice[0];
        eval += row1Coeffs[RowTerm.rowOrid(mover, enemy, 1)]
                + row1Coeffs[RowTerm.rowOrid(mover, enemy, 6)]
                + row1Coeffs[ColTerm.colOrid(mover, enemy, 1)]
                + row1Coeffs[ColTerm.colOrid(mover, enemy, 6)];

        final short[] row2Coeffs = slice[1];
        eval += row2Coeffs[RowTerm.rowOrid(mover, enemy, 2)]
                + row2Coeffs[RowTerm.rowOrid(mover, enemy, 5)]
                + row2Coeffs[ColTerm.colOrid(mover, enemy, 2)]
                + row2Coeffs[ColTerm.colOrid(mover, enemy, 5)];

        final short[] row3Coeffs = slice[2];
        eval += row3Coeffs[RowTerm.rowOrid(mover, enemy, 3)]
                + row3Coeffs[RowTerm.rowOrid(mover, enemy, 4)]
                + row3Coeffs[ColTerm.colOrid(mover, enemy, 3)]
                + row3Coeffs[ColTerm.colOrid(mover, enemy, 4)];

        if (iDebugEval > 1) {
            System.out.format("Rows & cols done. Value so far: %d.\n", eval);
        }

        final short[] diagonal8Coeffs = slice[3];
        eval += diagonal8Coeffs[OridTable.orid8(DiagonalTerm.diagonalInstance(mover, enemy, 0x8040201008040201L, 56))]
                + diagonal8Coeffs[OridTable.orid8(DiagonalTerm.diagonalInstance(mover, enemy, 0x0102040810204080L, 56))];

        final short[] diagonal7Coeffs = slice[4];
        eval += diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x4020100804020100L, 56))]
                + diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x0080402010080402L, 57))]
                + diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x0001020408102040L, 56))]
                + diagonal7Coeffs[OridTable.orid7(DiagonalTerm.diagonalInstance(mover, enemy, 0x0204081020408000L, 57))];

        final short[] diagonal6Coeffs = slice[5];
        eval += diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x2010080402010000L, 56))]
                + diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000804020100804L, 58))]
                + diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000010204081020L, 56))]
                + diagonal6Coeffs[OridTable.orid6(DiagonalTerm.diagonalInstance(mover, enemy, 0x0408102040800000L, 58))];

        final short[] diagonal5Coeffs = slice[6];
        eval += diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x1008040201000000L, 56))]
                + diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000008040201008L, 59))]
                + diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x0000000102040810L, 56))]
                + diagonal5Coeffs[OridTable.orid5(DiagonalTerm.diagonalInstance(mover, enemy, 0x0810204080000000L, 59))];

        if (iDebugEval > 1) {
            System.out.format("Straight lines done. Value so far: %d.\n", eval);
        }

        final short[] CornerTriangleCoeffs = slice[7];
        eval += CornerTriangleCoeffs[CornerTriangleTerm.orid(mover, enemy, false, false)]
                + CornerTriangleCoeffs[CornerTriangleTerm.orid(mover, enemy, true, false)]
                + CornerTriangleCoeffs[CornerTriangleTerm.orid(mover, enemy, false, true)]
                + CornerTriangleCoeffs[CornerTriangleTerm.orid(mover, enemy, true, true)];

        if (iDebugEval > 1) {
            System.out.format("Triangles done. Value so far: %d.\n", eval);
        }

        // Take apart packed information about pot mobilities
        int nPMO = BitBoardUtils.linearPotMob(mover, enemy);
        int nPMP = BitBoardUtils.linearPotMob(enemy, mover);

        if (iDebugEval > 1)
            System.out.format("Raw pot mobs: %d, %d\n", nPMO, nPMP);
        nPMO >>= 1;
        nPMP >>= 1;

        // pot mobility
        eval += slice[12][nPMP];
        eval += slice[13][nPMO];

        if (iDebugEval > 1)    {
            System.out.format("Potential mobility done. Value so far: %d.\n", eval);
        }

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

        final short[] Corner2x5Coeffs = slice[8];
        eval += c2x5(Corner2x5Coeffs, Corner2x5Term.leftInstance(moverRow0, enemyRow0, moverRow1, enemyRow1))
                + c2x5(Corner2x5Coeffs, Corner2x5Term.rightInstance(moverRow0, enemyRow0, moverRow1, enemyRow1))
                + c2x5(Corner2x5Coeffs, Corner2x5Term.leftInstance(moverRow7, enemyRow7, moverRow6, enemyRow6))
                + c2x5(Corner2x5Coeffs, Corner2x5Term.rightInstance(moverRow7, enemyRow7, moverRow6, enemyRow6))
                + c2x5(Corner2x5Coeffs, Corner2x5Term.leftInstance(moverCol0, enemyCol0, moverCol1, enemyCol1))
                + c2x5(Corner2x5Coeffs, Corner2x5Term.rightInstance(moverCol0, enemyCol0, moverCol1, enemyCol1))
                + c2x5(Corner2x5Coeffs, Corner2x5Term.leftInstance(moverCol7, enemyCol7, moverCol6, enemyCol6))
                + c2x5(Corner2x5Coeffs, Corner2x5Term.rightInstance(moverCol7, enemyCol7, moverCol6, enemyCol6));

        final short[] edge2XCoeffs = slice[9];
        eval += edge2XCoeffs[OridTable.orid10(Edge2XTerm.instance0(mover, enemy))]
                +  edge2XCoeffs[OridTable.orid10(Edge2XTerm.instance1(mover, enemy))]
                +  edge2XCoeffs[OridTable.orid10(Edge2XTerm.instance2(mover, enemy))]
                +  edge2XCoeffs[OridTable.orid10(Edge2XTerm.instance3(mover, enemy))];

        if (iDebugEval > 1)    {
            System.out.format("Corners done. Value so far: %d.\n", eval);
        }

        final short[] moverMobilityCoeffs = slice[10];
        eval += moverMobilityCoeffs[Long.bitCount(moverMoves)];

        final short[] enemyMobilityCoeffs = slice[11];
        eval += enemyMobilityCoeffs[Long.bitCount(enemyMoves)];

        if (iDebugEval > 1)    {
            System.out.format("Mobility done. Value so far: %d.\n", eval);
        }

        eval += slice[14][nEmpty&1];

        return eval;
    }

    private short c2x5(short[] coeffs, int instance) {
        if (iDebugEval > 1) {
            System.out.format("Config: %5d (%s) Value: %4d\n", instance, Base3.description(instance, 10), coeffs[instance]);
        }
        return coeffs[instance];
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
                    sb.append("final int moverRow" + i + " = BitBoardUtils.extractRow(mover, " + i + ");\n");
                    sb.append("final int enemyRow" + i + " = BitBoardUtils.extractRow(enemy, " + i + ");\n");
                    sb.append("final int moverCol" + i + " = BitBoardUtils.extractCol(mover, " + i + ");\n");
                    sb.append("final int enemyCol" + i + " = BitBoardUtils.extractCol(enemy, " + i + ");\n");
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
        final EvalStrategyF e = (EvalStrategyF) EvalStrategies.strategy("e");
        System.out.println(e.generateCode());
    }
}
