package com.welty.novello.eval;

/**
 */
public class CornerBlockTerm  extends Term {
    private final boolean top;
    private final boolean left;


    public CornerBlockTerm(boolean top, boolean left) {
        super(CornerBlockFeature.instance);
        this.top = top;
        this.left = left;
    }

    @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        final int moverRow = row(mover);
        final int enemyRow = row(enemy);
        return Base3.base2ToBase3(moverRow, enemyRow);
    }

    private int row(long mover) {
        if (left) {
            mover = Long.reverse(mover);
        }
        if (left ^ top) {
            mover = Long.reverseBytes(mover);
        }
        final long row = (mover & 0x7) | ((mover & 0x700) >>> 5) | ((mover & 0x70000) >> 10);
        return (int)row;
    }
}
