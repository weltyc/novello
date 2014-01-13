package com.welty.novello.eval;

import com.welty.novello.core.BitBoardUtils;

import static com.welty.novello.core.BitBoardUtils.*;

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

    @SuppressWarnings("OctalInteger")
    public static final CornerTerm2[] terms = {
            new CornerTerm2(000), new CornerTerm2(007), new CornerTerm2(070), new CornerTerm2(077)
    };

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

    public static final RowTerm[] internalTerms = {
            new RowTerm(1), new RowTerm(2), new RowTerm(3), new RowTerm(4), new RowTerm(5), new RowTerm(6)
    };

    static Feature getRowFeature(int row) {
        return features[row < 4 ? row : 7 - row];
    }

    private final int shift;

    RowTerm(int row) {
        super(getRowFeature(row));
        shift = row * 8;
    }

    public static int rowOrid(long mover, long enemy, int row) {
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
    public static final ColTerm[] internalTerms = {
            new ColTerm(1), new ColTerm(2), new ColTerm(3), new ColTerm(4), new ColTerm(5), new ColTerm(6)
    };

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

class UrdlTerm extends DiagonalTerm {
    public static UrdlTerm[] terms = new UrdlTerm[]{
            new UrdlTerm(0), new UrdlTerm(1), new UrdlTerm(-1), new UrdlTerm(2), new UrdlTerm(-2), new UrdlTerm(3), new UrdlTerm(-3), new UrdlTerm(4), new UrdlTerm(-4)
    };

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
    public static UldrTerm[] terms = new UldrTerm[]{
            new UldrTerm(0), new UldrTerm(1), new UldrTerm(-1), new UldrTerm(2), new UldrTerm(-2), new UldrTerm(3), new UldrTerm(-3), new UldrTerm(4), new UldrTerm(-4)
    };

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

@SuppressWarnings("OctalInteger")
class Edge2XTerm extends Term {
    private static final Feature feature = LinePatternFeatureFactory.of("edge2X", 10);
    static final Edge2XTerm[] terms = {new Edge2XTerm(0), new Edge2XTerm(1), new Edge2XTerm(2), new Edge2XTerm(3)};

    private final int direction;

    Edge2XTerm(int direction) {
        super(feature);
        this.direction = direction;
    }

    @Override
    public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        if (direction == 0) {
            return instance0(mover, enemy);
        } else if (direction == 1) {
            return instance1(mover, enemy);
        } else if (direction == 2) {
            return instance2(mover, enemy);
        } else if (direction == 3) {
            return instance3(mover, enemy);
        } else throw new IllegalStateException("not implemented");
    }

    static int instance0(long mover, long enemy) {
        return Base3.base2ToBase3(extractBottom(mover), extractBottom(enemy));
    }

    static int instance1(long mover, long enemy) {
        return Base3.base2ToBase3(extractTop(mover), extractTop(enemy));
    }

    static int instance2(long mover, long enemy) {
        return Base3.base2ToBase3(extractLeft(mover), extractLeft(enemy));
    }

    static int instance3(long mover, long enemy) {
        return Base3.base2ToBase3(extractRight(mover), extractRight(enemy));
    }

    static int extractBottom(long mover) {
        return (int) (((mover & Rank1) << 1) | ((mover & G7) >> 011) | ((mover & B7) >> 5));
    }

    static int extractTop(long mover) {
        return (int) (((mover & Rank8) >>> 067) | ((mover & G2) >> 061) | ((mover & B2) >> 055));
    }

    static int extractLeft(long mover) {
        final int leftRow = BitBoardUtils.extractCol(mover, 7);
        return (int) (2 * leftRow | ((mover & B2) >> 066) | ((mover & B7) >> 5));
    }

    static int extractRight(long mover) {
        final int rightRow = BitBoardUtils.extractCol(mover, 0);
        return (int) (2 * rightRow | ((mover & G2) >> 061) | (mover & G7));
    }

    @Override String oridGen() {
        return "OridTable.orid10(Edge2XTerm.instance" + direction + "(mover, enemy))";
    }
}

