package com.welty.novello.eval;

import com.welty.novello.core.BitBoardUtils;

import static com.welty.novello.core.BitBoardUtils.getBitAsInt;

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

    /**
     * Generate code to rapidly calculate the orid
     *
     * @return generated code fragment
     */
    String oridGen() {
        throw new IllegalStateException("not implemented for " + getClass().getSimpleName());
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
    @Override
    public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
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

    public CornerTerm2(int sq) {
        super(Features.corner2Feature);
        this.sq = sq;
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
    @Override
    public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        return orid(mover, enemy, moverMoves, enemyMoves, sq);
    }

    static int orid(long mover, long enemy, long moverMoves, long enemyMoves, int sq) {
        final int cornerOccupier = getBitAsInt(mover, sq) + 2 * getBitAsInt(enemy, sq);
        if (cornerOccupier > 0) {
            return cornerOccupier + 3;
        }
        final int cornerMobility = getBitAsInt(moverMoves, sq) + 2 * getBitAsInt(enemyMoves, sq);
        if (cornerMobility > 0) {
            return cornerMobility;
        } else {
            @SuppressWarnings("OctalInteger")
            final int xSq = sq ^ 011;
            final int xSquareOccupier = BitBoardUtils.getBitAsInt(mover, xSq) + 2 * BitBoardUtils.getBitAsInt(enemy, xSq);
            if (xSquareOccupier > 0) {
                return xSquareOccupier + 5;
            } else {
                return 0;
            }
        }
    }


    @Override String oridGen() {
        return "CornerTerm2.orid(mover, enemy, moverMoves, enemyMoves, " + sq + ")";
    }
}

class Terms {
    static final Term moverDisks = new Term(Features.moverDisks) {
        @Override
        public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return Long.bitCount(mover);
        }

        @Override String oridGen() {
            return "Long.bitCount(mover)";
        }
    };
    static final Term enemyDisks = new Term(Features.enemyDisks) {
        @Override
        public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return Long.bitCount(enemy);
        }

        @Override String oridGen() {
            return "Long.bitCount(enemy)";
        }
    };
    static final Term moverMobilities = new Term(Features.moverMobilities) {
        @Override
        public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return Long.bitCount(moverMoves);
        }

        @Override String oridGen() {
            return "Long.bitCount(moverMoves)";
        }
    };
    static final Term enemyMobilities = new Term(Features.enemyMobilities) {
        @Override
        public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            return Long.bitCount(enemyMoves);
        }

        @Override String oridGen() {
            return "Long.bitCount(enemyMoves)";
        }
    };

    static final Term moverPotMobs = new Term(Features.moverPotMobs) {
        @Override
        public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            final long empty = ~(mover | enemy);
            return Long.bitCount(BitBoardUtils.potMobs(mover, empty));
        }

        @Override String oridGen() {
            return "Long.bitCount(BitBoardUtils.potMobs(mover, empty))";
        }
    };
    static final Term enemyPotMobs = new Term(Features.enemyPotMobs) {
        @Override
        public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            final long empty = ~(mover | enemy);
            return Long.bitCount(BitBoardUtils.potMobs(enemy, empty));
        }

        @Override String oridGen() {
            return "Long.bitCount(BitBoardUtils.potMobs(enemy, empty))";
        }
    };
    static final Term moverPotMobs2 = new Term(Features.moverPotMobs2) {
        @Override
        public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            final long empty = ~(mover | enemy);
            return Long.bitCount(BitBoardUtils.potMobs2(mover, empty));
        }

        @Override String oridGen() {
            return "Long.bitCount(BitBoardUtils.potMobs2(mover, empty))";
        }
    };
    static final Term enemyPotMobs2 = new Term(Features.enemyPotMobs2) {
        @Override
        public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
            final long empty = ~(mover | enemy);
            return Long.bitCount(BitBoardUtils.potMobs2(enemy, empty));
        }

        @Override String oridGen() {
            return "Long.bitCount(BitBoardUtils.potMobs2(enemy, empty))";
        }

    };
}

class RowTerm extends Term {
    private static final Feature[] features = {
            LinePatternFeatureFactory.of("row0", 8),
            LinePatternFeatureFactory.of("row1", 8),
            LinePatternFeatureFactory.of("row2", 8),
            LinePatternFeatureFactory.of("row3", 8)
    };

    static Feature getRowFeature(int row) {
        return features[row < 4 ? row : 7 - row];
    }

    private final int shift;

    RowTerm(int row) {
        super(getRowFeature(row));
        shift = row * 8;
    }

    static int rowOrid(long mover, long enemy, int row) {
        return OridTable.orid8(BitBoardUtils.rowInstance(mover, enemy, row * 8));
    }

    @Override
    public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        final int shift = this.shift;
        return BitBoardUtils.rowInstance(mover, enemy, shift);
    }

    @Override String oridGen() {
        final int row = shift / 8;
        return "RowTerm.rowOrid(mover, enemy, " + row + ")";
    }
}

class ColTerm extends Term {

    private final int col;

    ColTerm(int col) {
        super(RowTerm.getRowFeature(col));
        this.col = col;
    }

    static int colOrid(long mover, long enemy, int col) {
        return OridTable.orid8(BitBoardUtils.colInstance(mover, enemy, col));
    }

    @Override
    public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        return BitBoardUtils.colInstance(mover, enemy, this.col);
    }


    @Override String oridGen() {
        return "ColTerm.colOrid(mover, enemy, " + col + ")";
    }
}

abstract class DiagonalTerm extends Term {
    static final Feature[] features = {
            LinePatternFeatureFactory.of("diagonal8", 8),
            LinePatternFeatureFactory.of("diagonal7", 7),
            LinePatternFeatureFactory.of("diagonal6", 6),
            LinePatternFeatureFactory.of("diagonal5", 5),
            LinePatternFeatureFactory.of("diagonal4", 4)
    };
    protected final int shift;
    protected final long mask;
    private final int diagonalLength;


    DiagonalTerm(int diagonal, long mask, int shift) {
        super(features[Math.abs(diagonal)]);
        this.mask = mask;
        this.shift = shift;
        diagonalLength = 8 - Math.abs(diagonal);
    }

    protected static long diagonalMask(int sq, int length, int dSq) {
        long mask = 0;
        for (int i = 0; i < length; i++) {
            mask |= 1L << sq;
            sq += dSq;
        }
        return mask;
    }

    /**
     * Quick orid calculation suitable for generated code
     *
     * @return text of quick orid calculation
     */
    String oridGen() {
        return String.format("OridTable.orid%d(DiagonalTerm.diagonalInstance(mover, enemy, 0x%016xL, %d))", diagonalLength, mask, shift);
    }

    @Override
    public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        final long mask = this.mask;
        final int shift = this.shift;
        return diagonalInstance(mover, enemy, mask, shift);
    }

    public static int diagonalInstance(long mover, long enemy, long mask, int shift) {
        final int moverDiagonal = extractDiagonal(mover, mask, shift);
        final int enemyDiagonal = extractDiagonal(enemy, mask, shift);
        return Base3.base2ToBase3(moverDiagonal, enemyDiagonal);
    }

    int extractDiagonal(long mover) {
        final long mask = this.mask;
        final int shift = this.shift;
        return extractDiagonal(mover, mask, shift);
    }

    private static int extractDiagonal(long mover, long mask, int shift) {
        return (int) ((mover & mask) * BitBoardUtils.HFile >>> shift);
    }
}

class UrdlTerm extends DiagonalTerm {
    UrdlTerm(int diagonal) {
        super(diagonal, urdlMask(diagonal), diagonal >= 0 ? 56 : 56 - diagonal);
    }

    static long urdlMask(int diagonal) {
        final int length;
        final int sq;

        if (diagonal >= 0) {
            length = 8 - diagonal;
            sq = length - 1;
        } else {
            length = 8 + diagonal;
            sq = 7 - 8 * diagonal;
        }
        final int dSq = 7;
        return diagonalMask(sq, length, dSq);
    }

}

class UldrTerm extends DiagonalTerm {
    UldrTerm(int diagonal) {
        super(diagonal, uldrMask(diagonal), shift(diagonal));
    }

    static long uldrMask(int diagonal) {
        final int length;
        final int sq;

        if (diagonal >= 0) {
            length = 8 - diagonal;
            sq = 8 * diagonal;
        } else {
            length = 8 + diagonal;
            sq = -diagonal;
        }
        return diagonalMask(sq, length, 9);
    }

    static int shift(int diagonal) {
        return diagonal >= 0 ? 56 : 56 - diagonal;
    }
}