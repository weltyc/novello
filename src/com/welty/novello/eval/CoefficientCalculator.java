package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.welty.novello.selfplay.SelfPlaySet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class CoefficientCalculator {
    /**
     * Generate coefficients for evaluation.
     * <p/>
     * This program generates a large number of PositionValues by self-play, then uses
     * those values to generate coefficients for the Evaluation function.
     */
    public static void main(String[] args) throws IOException {
        final EvalStrategy strategy = EvalStrategies.eval1;

        final List<PositionValue> pvs = loadPvs();
        System.out.println("a total of " + pvs.size() + " pvs are available.");
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            System.out.println("--- " + nEmpty + " ---");
            final Element[] elements = elementsFromPvs(pvs, nEmpty, strategy);
            System.out.println("estimating coefficients using " + elements.length + " positions");
            final double[] coefficients = estimateCoefficients(elements, strategy.nCoefficientIndices());
            strategy.dumpCoefficients(coefficients);

            // write to file
            final String filename = strategy.getFilename(nEmpty);
            try (final DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
                for (double c : coefficients) {
                    out.writeInt((int)Math.round(c));
                }
            }
        }
    }

    /**
     * @param elements indices for each position to match
     * @param nCoefficientIndices number of coefficients
     * @return optimal coefficients
     */
    static double[] estimateCoefficients(Element[] elements, int nCoefficientIndices) {
        final FunctionWithGradient f = new ErrorFunction(elements, nCoefficientIndices);
        return ConjugateGradientMethod.minimize(f);
    }

    /**
     * The function we are trying to minimize.
     * <p/>
     * We are minimizing the sum of squared errors of the elements.
     * For each element, the error is calculated as
     * target - sum over all indices of x[index]
     * <p/>
     * The gradient for index i is the sum over all elements e that contain index i of
     * 2*error_e
     */
    static class ErrorFunction implements FunctionWithGradient {
        private final Element[] elements;
        private final int nIndices;

        public ErrorFunction(Element[] elements, int nIndices) {
            this.elements = elements;
            this.nIndices = nIndices;
        }

        @Override public double[] minusGradient(double[] x) {
            Require.eq(x.length, "x length", nIndices);
            final double[] minusGradient = new double[x.length];
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
            return y;
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
    }

    /**
     * Select the pvs that will be used to generate coefficients at the given number of nEmpties and generate their Elements
     *
     * @param pvs                list of pvs at all empties
     * @param nEmpty             number of empties to generate coefficients for
     * @param strategy EvaluationFunction that is producing Elements.
     * @return list of selected Elements
     */
    private static Element[] elementsFromPvs(List<PositionValue> pvs, int nEmpty, EvalStrategy strategy) {
        final List<Element> res = new ArrayList<>();
        for (final PositionValue pv : pvs) {
            if (nEmpty == pv.nEmpty()) {
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
    public static List<PositionValue> loadPvs() {
        return SelfPlaySet.call();
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

