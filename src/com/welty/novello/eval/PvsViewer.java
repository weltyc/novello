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

import com.welty.novello.core.MeValue;

import java.io.IOException;
import java.util.List;

public class PvsViewer {
    public static void main(String[] args) throws IOException {
        final List<MeValue> pvs = new MvGenerator(EvalStrategies.strategy("e")).getMvs();
        int nPrinted = 0;
        for (MeValue pv : pvs) {
            if (Math.abs(pv.value) > 90*CoefficientCalculator.DISK_VALUE) {
                nPrinted++;
                if (nPrinted > 100) {
                    break;
                }
                System.out.println(pv);
            }
        }
    }
}
