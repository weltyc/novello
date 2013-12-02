package com.welty.novello.eval;

import com.orbanova.common.misc.ArrayTestCase;
import com.orbanova.common.misc.Vec;
import com.orbanova.common.ramfs.RamFileSystem;
import com.welty.novello.selfplay.Players;
import com.welty.novello.solver.BitBoard;
import com.welty.novello.solver.BitBoardUtils;

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
        assertEquals(expected, strategy.coefficientIndices(mover, enemy));
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
        assertEquals(new int[]{2, 2, 3}, strategy.coefficientIndices(3, 0));
    }

    public void testWriteRead() throws IOException {
        // specific strategy doesn't matter too much for this test, just want it to have
        // multiple terms and features.
        final EvalStrategy strategy = EvalStrategies.diagonal;
        final int nFeatures = strategy.nFeatures();

        final double[] coeffs = Vec.increasingDouble(0., 1. / CoefficientCalculator.DISK_VALUE, strategy.nCoefficientIndices());
        final RamFileSystem fs = new RamFileSystem();
        final Path coefficientDirectory = fs.getPath("coefficients");
        final int nEmpty = 12;
        strategy.writeSlice(nEmpty, coeffs, coefficientDirectory);

        final int[][] slice = strategy.readSlice(nEmpty, coefficientDirectory);
        assertEquals(nFeatures, slice.length);

        // test expected result for each feature
        int value = 0;
        for (int iFeature = 0; iFeature < nFeatures; iFeature++) {
            final Feature feature = strategy.getFeature(iFeature);
            final int nOrids = feature.nOrids();
            int[] expected = Vec.increasingInt(value, 1, nOrids);
            value += nOrids;

            assertEquals(expected, slice[iFeature]);
        }
    }

    public void testAllReflectionsHaveTheSameEval() {
        final Random random = new Random(1337);
        final Eval eval = Players.eval("7A");

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
        final BitBoard bitBoard = new BitBoard(mover, enemy, true);
        final int expected = bitBoard.eval(eval);
        for (int r = 1; r < 8; r++) {
            final BitBoard reflection = bitBoard.reflection(r);
            assertEquals(expected, reflection.eval(eval));
        }
    }

    /**
     * A test position with a small number of disks on the board
     */
    public static final BitBoard sparsePosition;

    /**
     * A test position with a large number of disks on the board
     */
    public static final BitBoard densePosition;

    static {
        final Random random = new Random(1337);

        // test with small # of disks on the board
        final long empty1 = random.nextLong() | random.nextLong();
        final long mover1 = random.nextLong() & ~empty1;
        final long enemy1 = ~(empty1 | mover1);
        sparsePosition = new BitBoard(mover1, enemy1, true);

        // test with large # of disks on the board
        final long empty2 = random.nextLong() | random.nextLong();
        final long mover2 = random.nextLong() & ~empty2;
        final long enemy2 = ~(empty2 | mover2);
        densePosition = new BitBoard(mover2, enemy2, true);
    }

    public void testAllReflectionsHaveSameOrids() {

        for (EvalStrategy strategy : EvalStrategies.knownStrategies()) {
            testAllReflectionsHaveTheSameOrids(strategy, sparsePosition);
            testAllReflectionsHaveTheSameOrids(strategy, densePosition);
            System.out.println("Success with EvalStrategy " + strategy);
        }
    }

    private void testAllReflectionsHaveTheSameOrids(EvalStrategy strategy, BitBoard position) {
        final long mover = position.mover();
        final long enemy = position.enemy();

        final int[] expected = strategy.coefficientIndices(mover, enemy);
        Arrays.sort(expected);
        for (int r=1; r<8; r++) {
            final long rMover = BitBoardUtils.reflection(mover, r);
            final long rEnemy = BitBoardUtils.reflection(enemy, r);
            final int[] actual = strategy.coefficientIndices(rMover, rEnemy);
            Arrays.sort(actual);
            if (!Arrays.equals(expected, actual)) {
                assertEquals(Arrays.toString(expected), Arrays.toString(actual));
            }
            assertEquals(strategy.toString(), expected, actual);
        }
    }
}
