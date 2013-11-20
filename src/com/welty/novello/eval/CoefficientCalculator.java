package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;
import com.welty.novello.selfplay.Bobby;
import com.welty.novello.selfplay.EvalPlayer;
import com.welty.novello.selfplay.Player;
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
        final EvalStrategy strategy = EvalStrategies.eval2;
        final double penalty = 10;

        final List<PositionValue> pvs = loadPvs();
        System.out.println("a total of " + pvs.size() + " pvs are available.");
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            System.out.println("--- " + nEmpty + " ---");
            final Element[] elements = elementsFromPvs(pvs, nEmpty, strategy);
            System.out.println("estimating coefficients using " + elements.length + " positions");
            final double[] coefficients = estimateCoefficients(elements, strategy.nCoefficientIndices(), penalty);
//            strategy.dumpCoefficients(coefficients);
//            System.out.println(Arrays.toString(Vec.last(coefficients, 4)));
            System.out.println("sum of coefficients squared = " + Vec.sumSq(coefficients));

            // write to file
            strategy.writeSlice(nEmpty, coefficients);
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
    static class ErrorFunction implements FunctionWithGradient {
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
            if (diff == 0 || diff == 2 || diff == -2) {
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
        final Player bobby = new Bobby();
        final Player diagonal = new EvalPlayer(EvalStrategies.diagonalStrategy);
        final ArrayList<PositionValue> pvs = new ArrayList<>();
        pvs.addAll(new SelfPlaySet(bobby, diagonal).call());
        pvs.addAll(new SelfPlaySet(bobby, bobby).call());
        pvs.addAll(new SelfPlaySet(diagonal, diagonal).call());
        return pvs;
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

