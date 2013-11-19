package com.welty.novello.eval;

import static com.welty.novello.solver.BitBoardUtils.getBitAsInt;

/**
 */
public class CornerTerm extends Term {
    private static final CornerFeature FEATURE = new CornerFeature();
    private final int sq;

    public CornerTerm(int sq) {
        super(FEATURE);
        this.sq = sq;
    }

    /**
     * Instance =
     * 4 mover occupies corner
     * 5 enemy occupies corner
     * <p/>
     * if corner is empty,
     * 0 nobody has access
     * 1 mover has access
     * 2 enemy has access
     * 3 both players have access
     *
     * @return the instance for this term
     */
    @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        final int base = bit(mover) + 2 * bit(enemy);
        if (base > 0) {
            return base + 3;
        }
        return bit(moverMoves) + 2 * bit(enemyMoves);
    }

    private int bit(long mover) {
        return getBitAsInt(mover, sq);
    }

}
