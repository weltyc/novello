/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.eval;

/**
 */
public class CoefficientViewer {
    public static void main(String[] args) {
        final int nEmpty = 4;

        dumpSlice("c6s", nEmpty, 500);
    }

    /**
     * Print out all coefficients with absolute value >= minValue
     */
    private static void dumpSlice(String eval, int nEmpty, int minValue) {
        final EvalStrategy strategy = EvalStrategies.strategy(eval.substring(0, 1));
        final String coeffSetName = eval.substring(1);
        final short[][] slice = strategy.readSlice(nEmpty, coeffSetName);
        System.out.println();
        System.out.println("=== Coefficients for " + strategy + coeffSetName + " with " + nEmpty + " empties ===");
        strategy.dumpCoefficients(slice, minValue);
    }
}
