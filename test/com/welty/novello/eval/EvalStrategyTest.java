package com.welty.novello.eval;

import com.orbanova.common.misc.ArrayTestCase;
import com.orbanova.common.misc.Vec;
import com.orbanova.common.ramfs.RamFileSystem;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Me;
import com.welty.novello.core.Position;
import com.welty.novello.selfplay.Players;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

/**
 */
public class EvalStrategyTest extends ArrayTestCase {
    public void testIndicesFromPosition() {
        final EvalStrategy strategy = EvalStrategies.eval1;

        // All 4 terms share the same feature
        assertEquals(1, strategy.nFeatures());

        // a sample position.
        // square 077 is mover, square 070 is enemy,
        // square 007 has mover access, square 000 has enemy access
        final long mover = 0x8001010101010100L;
        final long enemy = 0x0180808080808000L;
        final int[] expected = {2, 1, 5, 4};
        assertEquals(new PositionElement(expected, 0), strategy.coefficientIndices(mover, enemy, 0));
    }

    public void testFeatureCompression() {

        final EvalStrategy strategy = new EvalStrategy("test",
                TermTest.term1,
                TermTest.term1,
                TermTest.term2
        );

        assertEquals(2, strategy.nFeatures());
        assertEquals(new int[]{3, 2}, strategy.nOridsByFeature());
        assertEquals(TermTest.feature1.nOrids() + TermTest.feature2.nOrids(), strategy.nCoefficientIndices());
        assertEquals(new int[]{2, 2, 3}, strategy.coefficientIndices(3, 0, 0).indices);
    }

    public void testWriteRead() throws IOException {
        // specific strategy doesn't matter too much for this test, just want it to have
        // multiple terms and features.
        final EvalStrategy strategy = EvalStrategies.diagonal;
        final int nFeatures = strategy.nFeatures();

        final double[] coeffs = Vec.increasingDouble(0., 1., strategy.nCoefficientIndices());
        final RamFileSystem fs = new RamFileSystem();
        final Path coefficientDirectory = fs.getPath("coefficients");
        final int nEmpty = 12;
        strategy.writeSlice(nEmpty, coeffs, coefficientDirectory);

        final short[][] slice = strategy.readSlice(nEmpty, coefficientDirectory);
        assertEquals(nFeatures, slice.length);

        // test expected result for each feature
        int value = 0;
        for (int iFeature = 0; iFeature < nFeatures; iFeature++) {
            final Feature feature = strategy.getFeature(iFeature);
            final int nOrids = feature.nOrids();
            short start = (short) value;
            short[] increasing = new short[nOrids];
            for (short i = 0; i < nOrids; i++) {
                increasing[i] = start;
                start += 1;
            }
            value += nOrids;

            assertTrue(Arrays.equals(increasing, slice[iFeature]));
//            assertEquals(increasing, slice[iFeature]);
        }
    }

    public void testAllReflectionsHaveTheSameEval() {
        final Random random = new Random(1337);
        final Eval eval = Players.currentEval();

        // test with small # of disks on the board
        long empty = random.nextLong() | random.nextLong();
        long mover = random.nextLong() & ~empty;
        testAllReflectionsHaveTheSameEval(eval, mover, ~(empty | mover));

        // test with large # of disks on the board
        empty = random.nextLong() & random.nextLong();
        mover = random.nextLong() & ~empty;
        testAllReflectionsHaveTheSameEval(eval, mover, ~(empty | mover));
    }

    private void testAllReflectionsHaveTheSameEval(Eval eval, long mover, long enemy) {
        final Position position = new Position(mover, enemy, true);
        final int expected = eval.eval(position);
        for (int r = 1; r < 8; r++) {
            final Position reflection = position.reflection(r);
            assertEquals(expected, eval.eval(reflection));
        }
    }

    public void testAllReflectionsHaveSameOrids() {

        for (EvalStrategy strategy : EvalStrategies.knownStrategies()) {
            testAllReflectionsHaveTheSameOrids(strategy, Me.early);
            testAllReflectionsHaveTheSameOrids(strategy, Me.late);
        }
    }

    public void testGeneratedEvalMatchesEvalByTerms() {
        for (EvalStrategy strategy : EvalStrategies.knownStrategies()) {
            final short[][] randomCoefficients = strategy.generateRandomCoefficients();
            final CoefficientEval eval = new CoefficientEval(strategy, randomCoefficients);

            for (Me me : new Me[]{Me.early, Me.late}) {
                checkGeneratedEval(strategy, randomCoefficients, eval, me);
            }
        }
    }

    private static void checkGeneratedEval(EvalStrategy strategy, short[][] randomCoefficients, CoefficientEval eval, Me me) {
        final int evalByTerms = strategy.evalByTerms(me.mover, me.enemy, me.calcMoves(), me.enemyMoves(), randomCoefficients);
        final int generatedEval = eval.eval(me.mover, me.enemy);
        assertEquals(evalByTerms, generatedEval);
    }

    private void testAllReflectionsHaveTheSameOrids(EvalStrategy strategy, Me position) {
        final long mover = position.mover;
        final long enemy = position.enemy;

        PositionElement expected = strategy.coefficientIndices(mover, enemy, 0);
        expected.sortIndices();
        for (int r = 1; r < 8; r++) {
            final long rMover = BitBoardUtils.reflection(mover, r);
            final long rEnemy = BitBoardUtils.reflection(enemy, r);
            final PositionElement actual = strategy.coefficientIndices(rMover, rEnemy, 0);
            actual.sortIndices();
            if (!expected.equals(actual)) {
                assertEquals(expected.toString(), actual.toString());
            }
            assertEquals(strategy.toString(), expected, actual);
        }
    }
}
