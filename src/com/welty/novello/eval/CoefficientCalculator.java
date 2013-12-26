package com.welty.novello.eval;

import com.orbanova.common.math.function.oned.Function;
import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Utils;
import com.orbanova.common.misc.Vec;
import com.welty.novello.coca.ConjugateGradientMethod;
import com.welty.novello.coca.FunctionWithGradient;
import com.welty.novello.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 */
public class CoefficientCalculator {
    private static final Logger log = Logger.logger(CoefficientCalculator.class);

    /**
     * 1 disk is worth how many evaluation points?
     */
    public static final int DISK_VALUE = 100;
    private static final String target = "c6s";
    private static final String COEFF_SET_NAME = target.substring(1);
    private static final double PENALTY = .01;

    static final int[] nEmpties = {3, 4, 11, 12, 19, 20, 27, 28, 35, 36, 43, 44, 51, 52, 59, 60};

    /**
     * Generate coefficients for evaluation.
     * <p/>
     * This program generates a large number of PositionValues by self-play, then uses
     * those values to generate coefficients for the Evaluation function.
     * <p/>
     * With no arguments, this generates coefficients for the target EvalStrategy, but only for those slices that don't
     * already exist. If no slices can be generated, the program terminates early.
     * <p/>
     * With -h, display orid histograms without generating coefficients. This displays histograms regardless of whether
     * coefficients have already been generated.
     */
    public static void main(String[] args) throws Exception {
        final String shortOptions = NovelloUtils.getShortOptions(args);
        final boolean histogramOnly = shortOptions.contains("h");
        final EvalStrategy strategy = EvalStrategies.strategy(target.substring(0, 1));


        if (histogramOnly) {
            System.out.println("Not generating coefficients - histograms only");
        }

        // better to learn this now than after all the computations
        if (!histogramOnly) {
            try {
                strategy.checkSlicesCanBeCreated(COEFF_SET_NAME);
            } catch (IllegalArgumentException e) {
                log.warn("Coefficient set already exists.\n\nOverwriting coefficient files is not allowed.");
                System.exit(-1);
            }
        }

        final PvsGenerator pvsGenerator = new PvsGenerator(strategy);
        final List<PositionValue> pvs = pvsGenerator.loadOrCreatePvsx();

        System.out.format("a total of %,d pvs are available.%n", pvs.size());
        System.out.println();
        for (int nEmpty : nEmpties) {
            if (!histogramOnly && strategy.sliceExists(COEFF_SET_NAME, nEmpty)) {
                continue;
            }
            System.out.println("--- " + nEmpty + " ---");
            final PositionElement[] elements = elementsFromPvs(strategy, pvs, nEmpty);
            dumpElementDistribution(elements, strategy.nCoefficientIndices());
            if (!histogramOnly) {
                System.out.format("estimating coefficients using %,d positions\n", elements.length);
                final long t0 = System.currentTimeMillis();
                final double[] x = estimateCoefficients(elements, strategy.nCoefficientIndices(), strategy.nDenseWeights, PENALTY);
                final double[] coefficients = strategy.unpack(x);
                final long dt = System.currentTimeMillis() - t0;
                System.out.format("%,d ms elapsed\n", dt);
                System.out.format("sum of coefficients squared = %.3g\n", Vec.sumSq(coefficients));
                System.out.println();

                // write to file
                strategy.writeSlice(nEmpty, coefficients, COEFF_SET_NAME);
            }
        }
    }


    private static void dumpElementDistribution(PositionElement[] elements, int nIndices) {
        new OridHistogram(elements, nIndices).dump();
    }

    /**
     * @param elements      indices for each position to match
     * @param nCoefficients number of coefficients
     * @param nDenseWeights number of dense weights
     * @return optimal coefficients
     */
    static double[] estimateCoefficients(PositionElement[] elements, int nCoefficients, int nDenseWeights, double penalty) {
        final FunctionWithGradient f = new ErrorFunction(elements, nCoefficients, nDenseWeights, penalty);
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
        private final PositionElement[] elements;
        private final int nCoefficients;
        private final int nDenseWeights;
        private final double penalty;

        public ErrorFunction(PositionElement[] elements, int nCoefficients, int nDenseWeights, double penalty) {
            this.elements = elements;
            this.nCoefficients = nCoefficients;
            this.nDenseWeights = nDenseWeights;
            this.penalty = penalty;
        }

        @Override
        public double[] minusGradient(double[] x) {
            Require.eq(x.length, "x length", nDimensions());
            final double[] minusGradient = Vec.times(x, -2 * penalty);
            for (final PositionElement element : elements) {
                element.updateGradient(x, minusGradient);
            }
            return minusGradient;
        }

        @Override
        public double y(double[] x) {
            Require.eq(x.length, "x length", nDimensions());
            double y = 0;
            for (PositionElement element : elements) {
                final double error = element.error(x);
                y += error * error;
            }
            return y + penalty * Vec.sumSq(x);
        }

        @Override
        public int nDimensions() {
            return nCoefficients + nDenseWeights;
        }

        @NotNull
        public @Override Function getLineFunction(double[] x, double[] dx) {
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
                    final PositionElement element = elements[i];
                    errors[i] = element.error(x);
                    dErrors[i] = element.dError(dx);
                }
            }

            @Override
            public double y(double a) {
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
    private static PositionElement[] elementsFromPvs(EvalStrategy evalStrategy, List<PositionValue> pvs, int nEmpty) {
        final List<PositionElement> res = new ArrayList<>();
        for (final PositionValue pv : pvs) {
            final int diff = nEmpty - pv.nEmpty();
            if (!Utils.isOdd(diff) && diff >= -6 && diff <= 6) {
                final int target = clamp(pv.value, -64*DISK_VALUE, 64*DISK_VALUE);
                final PositionElement element = evalStrategy.coefficientIndices(pv.mover, pv.enemy, target);
                res.add(element);
            }
        }
        return res.toArray(new PositionElement[res.size()]);
    }

    private static int clamp(int x, int min, int max) {
        if (x<min) {
            return min;
        }
        if (x>max) {
            return max;
        }
        return x;
    }


}

