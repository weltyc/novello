package com.welty.novello.eval;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Data about a single position and value
 */
class PositionElement {
    final @NotNull int[] indices;
    private final int target;
    private final @NotNull float[] denseWeights;

    private static final float[] EMPTY_ARRAY = new float[0];

    PositionElement(int[] indices, int target) {
        this(indices, target, EMPTY_ARRAY);
    }

    public PositionElement(@NotNull int[] indices, int target, @NotNull float[] denseWeights) {
        this.indices = indices;
        this.target = target;
        this.denseWeights = denseWeights;
    }

    /**
     * Update the gradient of the optimization function (sum of squared errors)
     *
     * @param x             location at which to calculate the gradient
     * @param minusGradient (negative) gradient of the optimization function
     */
    void updateGradient(double[] x, double[] minusGradient) {
        final double error = error(x);
        for (int i : indices) {
            minusGradient[i] += 2 * error;
        }
        final int denseBase = minusGradient.length - denseWeights.length;
        for (int j = 0; j < denseWeights.length; j++) {
            minusGradient[denseBase + j] += 2 * error * denseWeights[j];
        }
    }

    void updateHistogram(int[] counts) {
        for (int index : indices) {
            counts[index]++;
        }
    }

    /**
     * Determine if one of this Element's indices is rare
     * <p/>
     * Rare means it has occurred fewer than maximum times,
     *
     * @param counts  histogram of index occurrences
     * @param maximum maximum number of times an index can occur and still be considered rare
     */
    boolean isRare(int[] counts, int maximum) {
        for (int index : indices) {
            if (counts[index] <= maximum) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate the error in the position value estimation
     * <p/>
     * error = target - &Sigma;<sub>i</sub> c<sub>i</sub> x<sub>i</sub>
     *
     * @param x vector of coefficient values
     * @return error
     */
    double error(double[] x) {
        double error = target;
        for (int i : indices) {
            error -= x[i];
        }
        final int denseBase = x.length - denseWeights.length;
        for (int j = 0; j < denseWeights.length; j++) {
            int i = denseBase + j;
            error -= x[i] * denseWeights[j];
        }
        return error;
    }

    /**
     * Calculate the directional derivative of error.
     *
     * @param deltaX direction
     * @return sum_i d(error)/dx_i * deltaX[i]
     */
    public double dError(double[] deltaX) {
        double dError = 0;
        for (int i : indices) {
            dError -= deltaX[i];
        }
        final int denseBase = deltaX.length - denseWeights.length;
        for (int j = 0; j < denseWeights.length; j++) {
            int i = denseBase + j;
            dError -= deltaX[i] * denseWeights[j];
        }
        return dError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PositionElement that = (PositionElement) o;

        if (target != that.target) return false;
        //noinspection SimplifiableIfStatement
        if (!Arrays.equals(denseWeights, that.denseWeights)) return false;
        return Arrays.equals(indices, that.indices);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(indices);
        result = 31 * result + target;
        result = 31 * result + Arrays.hashCode(denseWeights);
        return result;
    }

    /**
     * Sort indices.
     * <p/>
     * The only function this affects is equals, which will work correctly once the indices are sorted.
     */
    public void sortIndices() {
        Arrays.sort(indices);
    }

    @Override public String toString() {
        sortIndices();
        return Arrays.toString(indices);
    }
}
