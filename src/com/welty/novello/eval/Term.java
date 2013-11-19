package com.welty.novello.eval;

import com.welty.novello.solver.BitBoardUtils;

import static com.welty.novello.solver.BitBoardUtils.getBitAsInt;

/**
 */
public abstract class Term {
    private final Feature feature;

    protected Term(Feature feature) {
        this.feature = feature;
    }

    public abstract int instance(long mover, long enemy, long moverMoves, long enemyMoves);

    /**
     * Get the orid of the position.
     * <p/>
     * The moverMoves and enemyMoves can be calculated from mover and enemy; however
     * this is an expensive calculation. As a result the caller calculates the moves once for all terms.
     *
     * @return orid
     */
    public int orid(long mover, long enemy, long moverMoves, long enemyMoves) {
        return getFeature().orid(instance(mover, enemy, moverMoves, enemyMoves));
    }

    public Feature getFeature() {
        return feature;
    }
}

class CornerTerm extends Term {
    private final int sq;

    public CornerTerm(int sq) {
        super(Features.cornerFeature);
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

/**
 * A Term that gets its value from the disk pattern on the Upper Left / Down Right main diagonal
 */
class ULDRTerm extends Term {

    ULDRTerm() {
        super(Features.mainDiagonalFeature);
    }

    @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        // another idea to test: have instance = orid for this, since we're calculating instance by lookup table
        // anyway. This would decrease the amount of memory used by coeffs.
        // todo test that once eval is complete
        final long moverShift = shift(mover);
        final long enemyShift = shift(enemy);
        // todo do this by lookup table
        return Base3.base2ToBase3((int) (moverShift & 0xFF), (int) (enemyShift & 0xFF));
    }

    /**
     * // this funky shift moves the long diagonal down to bits 0-7.
     * // can do it in fewer shifts but with increased latency by doing it O(log(n)) - not sure if this helps
     * // or hurts performance.
     * // todo test O(log(n)) shifting once eval is complete
     *
     * @param bb bitboard
     * @return shifted board.
     */
    @SuppressWarnings("OctalInteger")
    static long shift(long bb) {
        return ((bb & BitBoardUtils.AFile) >>> 070) |
                ((bb & BitBoardUtils.BFile) >>> 060) |
                ((bb & BitBoardUtils.CFile) >>> 050) |
                ((bb & BitBoardUtils.DFile) >>> 040) |
                ((bb & BitBoardUtils.EFile) >>> 030) |
                ((bb & BitBoardUtils.FFile) >>> 020) |
                ((bb & BitBoardUtils.GFile) >>> 010) |
                ((bb & BitBoardUtils.HFile));
    }
}

/**
 * A Term that gets its value from the disk pattern on the Upper Right / Down Left main diagonal
 */
class URDLTerm extends Term {
    URDLTerm() {
        super(Features.mainDiagonalFeature);
    }

    @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        // another idea to test: have instance = orid for this, since we're calculating instance by lookup table
        // anyway. This would decrease the amount of memory used by coeffs.
        // todo test that once eval is complete
        final long moverShift = shift(mover);
        final long enemyShift = shift(enemy);
        // todo do this by lookup table
        return Base3.base2ToBase3((int) (moverShift & 0xFF), (int) (enemyShift & 0xFF));
    }

    /**
     * // this funky shift moves the long diagonal down to bits 0-7.
     * // can do it in fewer shifts but with increased latency by doing it O(log(n)) - not sure if this helps
     * // or hurts performance.
     * // todo test O(log(n)) shifting once eval is complete
     *
     * @param bb bitboard
     * @return shifted board.
     */
    @SuppressWarnings("OctalInteger")
    static long shift(long bb) {
        return ((bb & BitBoardUtils.HFile) >>> 070) |
                ((bb & BitBoardUtils.GFile) >>> 060) |
                ((bb & BitBoardUtils.FFile) >>> 050) |
                ((bb & BitBoardUtils.EFile) >>> 040) |
                ((bb & BitBoardUtils.DFile) >>> 030) |
                ((bb & BitBoardUtils.CFile) >>> 020) |
                ((bb & BitBoardUtils.BFile) >>> 010) |
                ((bb & BitBoardUtils.AFile));
    }
}