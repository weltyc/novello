package com.welty.novello.eval;

import com.orbanova.common.math.function.oned.Function;
import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Utils;
import com.orbanova.common.misc.Vec;
import com.welty.novello.core.MutableGame;
import com.welty.novello.selfplay.*;
import com.welty.novello.core.Position;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 */
public class CoefficientCalculator {
    private static final Logger log = Logger.logger(CoefficientCalculator.class);

    /**
     * 1/randomFraction of the self-play positions will have all subpositions valued and used in the regression
     */
    private static final int randomFraction = 1;
    /**
     * 1 disk is worth how many evaluation points?
     */
    public static final int DISK_VALUE = 100;
    private static final String target = "a1";
    private static final EvalStrategy STRATEGY = EvalStrategies.strategy(target.substring(0, 1));
    private static final String COEFF_SET_NAME = target.substring(1);
    private static final double PENALTY = 100;
    private static final Player PLAYOUT_PLAYER = Players.player("7A:2");

    /**
     * Generate coefficients for evaluation.
     * <p/>
     * This program generates a large number of PositionValues by self-play, then uses
     * those values to generate coefficients for the Evaluation function.
     */
    public static void main(String[] args) throws Exception {

        // better to learn this now than after all the computations
        try {
            STRATEGY.checkSlicesCanBeCreated(COEFF_SET_NAME);
        } catch (IllegalArgumentException e) {
            System.err.println("Coefficient set already exists.\n\nOverwriting coefficient files is not allowed.");
            System.exit(-1);
        }

        final List<PositionValue> pvs = loadOrCreatePvs();

        System.out.format("a total of %,d pvs are available.%n", pvs.size());
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            if (STRATEGY.sliceExists(COEFF_SET_NAME, nEmpty)) {
                continue;
            }
            System.out.println();
            System.out.println("--- " + nEmpty + " ---");
            final Element[] elements = elementsFromPvs(pvs, nEmpty);
            dumpElementDistribution(elements, STRATEGY.nCoefficientIndices());
            System.out.format("estimating coefficients using %,d positions\n", elements.length);
            final long t0 = System.currentTimeMillis();
            final double[] coefficients = estimateCoefficients(elements, STRATEGY.nCoefficientIndices(), PENALTY);
            final long dt = System.currentTimeMillis() - t0;
            System.out.format("%,d ms elapsed\n", dt);
            System.out.format("sum of coefficients squared = %.3g\n", Vec.sumSq(coefficients));

            // write to file
            STRATEGY.writeSlice(nEmpty, coefficients, COEFF_SET_NAME);
        }
    }

    private static void dumpElementDistribution(Element[] elements, int nIndices) {
        final int[] counts = new int[nIndices];
        for (Element element : elements) {
            for (int index : element.indices) {
                counts[index]++;
            }
        }
        final int[] histogram = new int[12];
        for (int count : counts) {
            int lg = 32 - Integer.numberOfLeadingZeros(count);
            if (lg > 11) {
                lg = 11;
            }
            histogram[lg]++;
        }
        System.out.println("== instance counts ==");
        for (int i = 0; i < histogram.length; i++) {
            final int h = histogram[i];
            if (h > 0) {
                System.out.format("%,8d coefficients occurred %s%n", h, rangeText(i));
            }
        }
        System.out.println();
    }

    private static String rangeText(int i) {
        if (i == 0) {
            return "0 times";
        }
        if (i == 1) {
            return "1 time";
        }
        int min = 1 << (i - 1);
        if (i == 11) {
            return min + "+ times";
        }
        int max = (1 << i) - 1;
        return min + "-" + max + " times";
    }

    private static List<PositionValue> loadOrCreatePvs() throws IOException {
        final String playerComponent = PLAYOUT_PLAYER.toString().replace(':', '-');
        final Path pvFile = Paths.get("c:/temp/novello/" + playerComponent + ".pvs");
        if (!Files.exists(pvFile)) {
            createPvs(pvFile);
        }
        System.out.println("Loading pvs from " + pvFile + "...");
        final List<PositionValue> pvs;
        final long t0 = System.currentTimeMillis();
        pvs = new ObjectFeed<>(pvFile, PositionValue.deserializer).asList();
        // Ram is tight... free some up
        ((ArrayList) pvs).trimToSize();

        final long dt = System.currentTimeMillis() - t0;
        System.out.format("...  loaded %,d pvs in %.3f s\n", pvs.size(), dt * .001);
        return pvs;
    }

    /**
     * @param elements            indices for each position to match
     * @param nCoefficientIndices number of coefficients
     * @return optimal coefficients
     */
    static double[] estimateCoefficients(Element[] elements, int nCoefficientIndices, double penalty) {
        final FunctionWithGradient f = new ErrorFunction(elements, nCoefficientIndices, penalty);
        return ConjugateGradientMethod.minimize(f);
    }

    /**
     * The function we are trying to minimize.
     * <p/>
     * This function is the sum of two components: the sum of squared errors plus
     * a PENALTY term which forces the coefficients to 0.
     * <p/>
     * Let
     * <ul>
     * <li>i be an index into the coefficient array</li>
     * <li>x[i] be the coefficient for index i</li>
     * <li>e be an element in the elements list</li>
     * <li>e.N[i] be the number of times pattern i occurs in the element</li>
     * <li>p be the size of the PENALTY</li>
     * </ul>
     * An element's error, e.error = e.target - &Sigma;<sub>i</sub>e.N[i]x[i]
     * <p/>
     * The sum of squared errors is<br/>
     * &Sigma;<sub>e</sub>e.error^2<br/>
     * <p/>
     * Its gradient for index i is
     * -2 &Sigma;<sub>e</sub>error*e.N[i]
     * <p/>
     * The PENALTY term is<br/>
     * p &Sigma;<sub>i</sub>x[i]^2
     * <p/>
     * Its gradient for index i is
     * 2 p x[i]
     */
    static class ErrorFunction extends FunctionWithGradient {
        private final Element[] elements;
        private final int nIndices;
        private final double penalty;

        public ErrorFunction(Element[] elements, int nIndices, double penalty) {
            this.elements = elements;
            this.nIndices = nIndices;
            this.penalty = penalty;
        }

        @Override public double[] minusGradient(double[] x) {
            Require.eq(x.length, "x length", nIndices);
            final double[] minusGradient = Vec.times(x, -2 * penalty);
            for (final Element element : elements) {
                final double error = error(x, element);
                for (int i : element.indices) {
                    minusGradient[i] += 2 * error;
                }
            }
            return minusGradient;
        }

        @Override public double y(double[] x) {
            double y = 0;
            for (Element element : elements) {
                final double error = error(x, element);
                y += error * error;
            }
            return y + penalty * Vec.sumSq(x);
        }

        @Override public int nDimensions() {
            return nIndices;
        }

        private static double error(double[] x, Element element) {
            double error = element.target;
            for (int i : element.indices) {
                error -= x[i];
            }
            return error;
        }

        @NotNull @Override Function getLineFunction(double[] x, double[] dx) {
            return new LineFunction(x, dx);
        }

        /**
         * Precomputes some stuff to speed up line minimization
         */
        class LineFunction implements Function {
            final double[] errors;
            final double[] dErrors;
            private final double[] x;
            private final double[] dx;

            public LineFunction(double[] x, double[] dx) {
                this.x = x;
                this.dx = dx;
                errors = new double[elements.length];
                dErrors = new double[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    final Element element = elements[i];
                    errors[i] = error(x, element);
                    for (int j : element.indices) {
                        dErrors[i] -= dx[j];
                    }
                }
            }

            @Override public double y(double a) {
                double y = 0;
                for (int i = 0; i < errors.length; i++) {
                    final double error = errors[i] + dErrors[i] * a;
                    y += error * error;
                }
                double p = 0;
                for (int i = 0; i < x.length; i++) {
                    final double coeff = x[i] + a * dx[i];
                    p += coeff * coeff;
                }
                return y + penalty * p;
            }
        }
    }

    /**
     * Select the pvs that will be used to generate coefficients at the given number of nEmpties and generate their Elements
     *
     * @param pvs    list of pvs at all empties
     * @param nEmpty number of empties to generate coefficients for
     * @return list of selected Elements
     */
    private static Element[] elementsFromPvs(List<PositionValue> pvs, int nEmpty) {
        final List<Element> res = new ArrayList<>();
        for (final PositionValue pv : pvs) {
            final int diff = nEmpty - pv.nEmpty();
            if (!Utils.isOdd(diff) && diff >= -6 && diff <= 6) {
                final int[] indices = CoefficientCalculator.STRATEGY.coefficientIndices(pv.mover, pv.enemy);
                res.add(new Element(indices, pv.value));
            }
        }
        return res.toArray(new Element[res.size()]);
    }

    /**
     * Generate a set of PositionValues for analysis and write them to a file.
     *
     * @param pvFile path to the file to be written.
     */
    private static void createPvs(Path pvFile) throws IOException {
        log.info("Creating Pvs in " + pvFile + " ...");
        final ArrayList<PositionValue> pvs = new ArrayList<>();
        pvs.addAll(new SelfPlaySet(PLAYOUT_PLAYER, PLAYOUT_PLAYER, 0, false).call().pvs);
        try (final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(pvFile)))) {
            writeRandomSubpositions(pvs, out);
        }
    }

    /**
     * Value randomly selected positions and write out their pvs.
     * <p/>
     * Pick a random selection of positions from the list; for each chosen position, play a game from that
     * position and append the first two positions from that game to out.
     * <p/>
     * The positions are valued by the playout. The playout is played by eval4/A, which is the current best
     * evaluator.
     * <p/>
     * 1/randomFraction of the positions will be chosen
     * <p/>
     * This function does NOT close the DataOutputStream.
     *
     * @param pvs positions that might get chosen
     */
    private static void writeRandomSubpositions(List<PositionValue> pvs, DataOutputStream out) throws IOException {
        final Player player = PLAYOUT_PLAYER;
        final Random random = new Random(1337);
        int nextMessage = 50000;
        int nWritten = 0;

        log.info(String.format("%,d original positions", pvs.size()));
        for (PositionValue pv : pvs) {
            if (random.nextInt(randomFraction) == 0) {
                nWritten += writeSubPositions(out, pv.mover, pv.enemy, player);
            }
            if (nWritten >= nextMessage) {
                log.info(String.format(" Added %,d random positions", nWritten));
                nextMessage += 50000;
            }
        }
    }

    /**
     * Write the first two positions of this game to out.
     *
     * @return number of positions written
     */
    private static int writeSubPositions(DataOutputStream out, long mover, long enemy, Player player) throws IOException {
        int nWritten = 0;
        final Position pos = new Position(mover, enemy, true);
        long moves = pos.calcMoves();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            moves ^= 1L << sq;
            final Position subPos = pos.play(sq);
            final MutableGame game = new SelfPlayGame(subPos, player, player, "", false, 0).call();
            final List<PositionValue> gamePvs = game.calcPositionValues();
            final List<PositionValue> toAdd = gamePvs.subList(0, Math.min(2, gamePvs.size()));
            for (PositionValue pv : toAdd) {
                pv.write(out);
                nWritten++;
            }
        }
        return nWritten;
    }
}


class Element {
    final int[] indices;
    final int target;

    Element(int[] indices, int target) {
        this.indices = indices;
        this.target = target;
    }
}
