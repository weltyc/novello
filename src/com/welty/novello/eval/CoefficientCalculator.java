package com.welty.novello.eval;

import com.orbanova.common.math.function.oned.Function;
import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Utils;
import com.orbanova.common.misc.Vec;
import com.welty.novello.selfplay.*;
import com.welty.novello.solver.BitBoard;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
    private static final int randomFraction = 10;
    /**
     * 1 disk is worth how many evaluation points?
     */
    public static final int DISK_VALUE = 100;

    /**
     * Generate coefficients for evaluation.
     * <p/>
     * This program generates a large number of PositionValues by self-play, then uses
     * those values to generate coefficients for the Evaluation function.
     */
    public static void main(String[] args) throws IOException {
        final EvalStrategy strategy = EvalStrategies.eval5;
        final double penalty = 10000;
        final String coeffSetName = "A";

        final List<PositionValue> pvs = loadPvs(Players.eval4);
        System.out.format("a total of %,d pvs are available.%n", pvs.size());
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            System.out.println();
            System.out.println("--- " + nEmpty + " ---");
            final Element[] elements = elementsFromPvs(pvs, nEmpty, strategy);
            System.out.println("estimating coefficients using " + elements.length + " positions");
            final long t0 = System.currentTimeMillis();
            final double[] coefficients = estimateCoefficients(elements, strategy.nCoefficientIndices(), penalty);
            final long dt = System.currentTimeMillis() - t0;
            System.out.println(dt + " ms elapsed");
            System.out.println("sum of coefficients squared = " + Vec.sumSq(coefficients));

            // write to file
            strategy.writeSlice(nEmpty, coefficients, coeffSetName);
        }
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
     * a penalty term which forces the coefficients to 0.
     * <p/>
     * Let
     * <ul>
     * <li>i be an index into the coefficient array</li>
     * <li>x[i] be the coefficient for index i</li>
     * <li>e be an element in the elements list</li>
     * <li>e.N[i] be the number of times pattern i occurs in the element</li>
     * <li>p be the size of the penalty</li>
     * </ul>
     * An element's error, e.error = e.target - &Sigma;<sub>i</sub>e.N[i]x[i]
     * <p/>
     * The sum of squared errors is<br/>
     * &Sigma;<sub>e</sub>e.error^2<br/>
     * <p/>
     * Its gradient for index i is
     * -2 &Sigma;<sub>e</sub>error*e.N[i]
     * <p/>
     * The penalty term is<br/>
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

        @NotNull @Override protected Function getLineFunction(double[] x, double[] dx) {
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
     * @param pvs      list of pvs at all empties
     * @param nEmpty   number of empties to generate coefficients for
     * @param strategy EvaluationFunction that is producing Elements.
     * @return list of selected Elements
     */
    private static Element[] elementsFromPvs(List<PositionValue> pvs, int nEmpty, EvalStrategy strategy) {
        final List<Element> res = new ArrayList<>();
        for (final PositionValue pv : pvs) {
            final int diff = nEmpty - pv.nEmpty();
            if (!Utils.isOdd(diff) && diff >= -6 && diff <= 6) {
                final int[] indices = strategy.coefficientIndices(pv.mover, pv.enemy);
                res.add(new Element(indices, pv.value));
            }
        }
        return res.toArray(new Element[res.size()]);
    }

    /**
     * Get a set of PositionValues for analysis
     *
     * @return the positionValues
     */
    public static List<PositionValue> loadPvs(Player... players) {
        final ArrayList<PositionValue> pvs = new ArrayList<>();
        for (int i = 0; i < players.length; i++) {
            for (int j = 0; j <= i; j++) {
                pvs.addAll(new SelfPlaySet(players[i], players[j], 0).call());
            }
        }

        pvs.addAll(randomSubpositions(pvs));
        return pvs;
    }

    /**
     * Pick a random selection of positions from the list; for each chosen position, play a game from that
     * position and add the first two positions from that game to the result.
     * <p/>
     * The positions are valued by the playout. The playout is played by eval4/A, which is the current best
     * evaluator.
     * <p/>
     * 1/randomFraction of the positions will be chosen
     *
     * @param pvs positions that might get chosen
     * @return random selection
     */
    private static List<PositionValue> randomSubpositions(List<PositionValue> pvs) {
        final Player player = Players.eval4;
        final Random random = new Random(1337);
        int nextMessage = 50000;

        log.info(String.format("%,d original positions", pvs.size()));
        final List<PositionValue> result = new ArrayList<>();
        for (PositionValue pv : pvs) {
            if (random.nextInt(randomFraction)==0) {
                addSubPositions(result, pv.mover, pv.enemy, player);
            }
            if (result.size() >= nextMessage) {
                log.info(String.format(" Added %,d random positions", result.size()));
                nextMessage += 50000;
            }
        }
        return result;
    }

    /**
     * Add all subpositions of this position to the result list
     */
    private static void addSubPositions(List<PositionValue> result, long mover, long enemy, Player player) {
        final BitBoard pos = new BitBoard(mover, enemy, true);
        long moves = pos.calcMoves();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            moves ^= 1L<<sq;
            final BitBoard subPos = pos.play(sq);
            final SelfPlayGame.Result gameResult = new SelfPlayGame(subPos, player, player, false).call();
            final List<PositionValue> gamePvs = gameResult.getPositionValues();
            result.addAll(gamePvs.subList(0, Math.min(2, gamePvs.size())));
        }
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
