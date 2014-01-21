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
