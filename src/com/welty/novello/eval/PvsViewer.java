package com.welty.novello.eval;

import com.welty.novello.core.PositionValue;

import java.io.IOException;
import java.util.List;

public class PvsViewer {
    public static void main(String[] args) throws IOException {
        final List<PositionValue> pvs = PvsGenerator.loadOrCreatePvs("c5s-10");
        int nPrinted = 0;
        for (PositionValue pv : pvs) {
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
