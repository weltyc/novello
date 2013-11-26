package com.welty.novello.eval;

import com.welty.novello.solver.BitBoardUtils;

import static com.welty.novello.solver.BitBoardUtils.getBitAsInt;

/**
 */
abstract class Term {
    private final Feature feature;

    Term(Feature feature) {
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

class CornerTerm2 extends Term {
    private final int sq;
    private final int xSq;

    public CornerTerm2(int sq) {
        super(Features.corner2Feature);
        this.sq = sq;
        //noinspection OctalInteger
        this.xSq = sq^011;
    }

    /**
     * Instance =
     * 4 mover occupies corner
     * 5 enemy occupies corner
     * <p/>
     * if corner is empty,
     * 1 mover has access
     * 2 enemy has access
     * 3 both players have access
     * <p/>
     * if nobody has access,
     * 0 = empty x-square
     * 6 = mover on x-square,
     * 7 = enemy on x-square
     *
     * @return the instance for this term
     */
    @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        final int cornerOccupier = bit(mover) + 2 * bit(enemy);
        if (cornerOccupier > 0) {
            return cornerOccupier + 3;
        }
        final int cornerMobility = bit(moverMoves) + 2 * bit(enemyMoves);
        if (cornerMobility > 0) {
            return cornerMobility;
        }
        else {
            final int xSquareOccupier = BitBoardUtils.getBitAsInt(mover, xSq) + 2*BitBoardUtils.getBitAsInt(enemy, xSq);
            if (xSquareOccupier>0) {
                return xSquareOccupier + 5;
            }
            else {
                return 0;
            }
        }
    }

    private int bit(long mover) {
        return getBitAsInt(mover, sq);
    }
}

class Terms {
    static final Term moverDisks = new Term(Features.moverDisks) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return Long.bitCount(mover);
        }
    };
    static final Term enemyDisks = new Term(Features.enemyDisks) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return Long.bitCount(enemy);
        }
    };
    static final Term moverMobilities = new Term(Features.moverMobilities) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return Long.bitCount(moverMoves);
        }
    };
    static final Term enemyMobilities = new Term(Features.enemyMobilities) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return Long.bitCount(enemyMoves);
        }
    };
    static final Term moverPotMobs = new Term(Features.moverPotMobs) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            final long empty = ~(mover|enemy);
            return Long.bitCount(BitBoardUtils.potMobs(mover, empty));
        }
    };
    static final Term enemyPotMobs = new Term(Features.enemyPotMobs) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            final long empty = ~(mover|enemy);
            return Long.bitCount(BitBoardUtils.potMobs(enemy, empty));
        }
    };
    static final Term moverPotMobs2 = new Term(Features.moverPotMobs2) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            final long empty = ~(mover|enemy);
            return Long.bitCount(BitBoardUtils.potMobs2(mover, empty));
        }
    };
    static final Term enemyPotMobs2 = new Term(Features.enemyPotMobs2) {
        @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            final long empty = ~(mover|enemy);
            return Long.bitCount(BitBoardUtils.potMobs2(enemy, empty));
        }
    };
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
    private static long shift(long bb) {
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

class RowTerm extends Term {
    private static final Feature[] features = {
            LinePatternFeatureFactory.of("row 0", 8),
            LinePatternFeatureFactory.of("row 1", 8),
            LinePatternFeatureFactory.of("row 2", 8),
            LinePatternFeatureFactory.of("row 3", 8)
    };

    static Feature getRowFeature(int row) {
        return features[row < 4 ? row : 7 - row];
    }

    private final int shift;

    RowTerm(int row) {
        super(getRowFeature(row));
        shift = row * 8;
    }

    @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        return Base3.base2ToBase3(0xFF & (int) (mover >>> shift), 0xFF & (int) (enemy >>> shift));
    }
}

class ColTerm extends Term {
    private final int shift;

    ColTerm(int col) {
        super(RowTerm.getRowFeature(col));
        shift = col * 8;
    }

    @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        final long rMover = BitBoardUtils.reflectDiagonally(mover);
        final long rEnemy= BitBoardUtils.reflectDiagonally(enemy);
        return Base3.base2ToBase3(0xFF & (int) (rMover >>> shift), 0xFF & (int) (rEnemy >>> shift));
    }
}