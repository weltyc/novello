package com.welty.novello.eval;

/**
 */
class CornerBlockTerm extends Term {
    public static CornerBlockTerm[] terms = new CornerBlockTerm[]{new CornerBlockTerm(false, false), new CornerBlockTerm(false, true), new CornerBlockTerm(true, false), new CornerBlockTerm(true, true)};

    private final boolean top;
    private final boolean left;


    public CornerBlockTerm(boolean top, boolean left) {
        super(CornerBlockFeature.instance);
        this.top = top;
        this.left = left;
    }

    @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        return instance(mover, enemy, this.left, this.top);
    }

    public static int orid(long mover, long enemy, boolean left, boolean top) {
        return CornerBlockFeature.instance.orid(instance(mover, enemy, left, top));
    }

    private static int instance(long mover, long enemy, boolean left, boolean top) {
        final int moverRow = row(mover, left, top);
        final int enemyRow = row(enemy, left, top);
        return Base3.base2ToBase3(moverRow, enemyRow);
    }

    private static int row(long mover, boolean left, boolean top) {
        if (left) {
            mover = Long.reverse(mover);
        }
        if (left ^ top) {
            mover = Long.reverseBytes(mover);
        }
        final long row = (mover & 0x7) | ((mover & 0x700) >>> 5) | ((mover & 0x70000) >> 10);
        return (int) row;
    }

    @Override String oridGen() {
        return "CornerBlockTerm.orid(mover, enemy, " + left + ", " + top + ")";
    }
}
