package com.welty.novello.eval;

import com.welty.novello.core.BitBoardUtils;

/**
 */
public abstract class DiagonalTerm extends Term {
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
