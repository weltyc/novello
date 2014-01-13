package com.welty.novello.eval;

/**
 */
class CornerTriangleTerm extends Term {
    private final boolean top;
    private final boolean left;

    public static final CornerTriangleTerm[] terms = {
            new CornerTriangleTerm(false, false),
            new CornerTriangleTerm(false, true),
            new CornerTriangleTerm(true, false),
            new CornerTriangleTerm(true, true)
    };

    public CornerTriangleTerm(boolean top, boolean left) {
        super(CornerTriangleFeature.instance);
        this.top = top;
        this.left = left;
    }

    @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        return instance(mover, enemy, this.left, this.top);
    }

    public static int orid(long mover, long enemy, boolean left, boolean top) {
        return CornerTriangleFeature.instance.orid(instance(mover, enemy, left, top));
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
        final long row = (mover & 0xF) | ((mover & 0x700) >>> 4) | ((mover & 0x30000) >>> 9) | (mover & 0x1000000) >>> 15;
        return (int) row;
    }

    @Override String oridGen() {
        return "CornerTriangleTerm.orid(mover, enemy, " + left + ", " + top + ")";
    }
}
