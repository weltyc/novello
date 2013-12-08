package com.welty.novello.eval;

/**
 */
public class DiskEval extends Eval {
    @Override public int eval(long mover, long enemy) {
        return CoefficientCalculator.DISK_VALUE * (Long.bitCount(mover)-Long.bitCount(enemy));
    }
}
