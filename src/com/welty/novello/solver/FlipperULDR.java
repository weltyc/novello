package com.welty.novello.solver;

import static com.welty.novello.solver.BitBoardUtils.bit;
import static com.welty.novello.solver.BitBoardUtils.col;
import static com.welty.novello.solver.BitBoardUtils.row;

/**
 * Each square has a FlipperULDR. The FlipperULDR calculates flips when a disk is placed on its square.
 *
 * Uses kindergarten bitboards.
 * See <a href="http://chessprogramming.wikispaces.com/Kindergarten+Bitboards">chessprogramming wiki</a>.
 */
final class FlipperULDR {
    /**
     * Bitboard containing the square's diagonal (including the square).
     */
    private final long diagonal;

    /**
     * Amount to shift by, when getting kindergarten index
     */
    private final long shift;

    /**
     * Length of diagonal
     */
    private final int length;

    /**
     * Precomputed table of which disks are flipped
     */
    private final long[] flipTable;

    FlipperULDR(int sq) {
        final int row = row(sq);
        final int col = col(sq);

        diagonal = diagonal(sq, row, col);
        length = Long.bitCount(diagonal);

        final int diff = col-row;
        shift = diff <= 0 ? 56 : 56 + diff;

        flipTable = new long[1<<2*length];
        final long placement = 1L<<sq;
        assert (placement&diagonal)!=0;
        initFlipTable(placement, diagonal &~placement, 0L, 0L);
    }

    /**
     * Recursive flipTable initialization.
     *
     * @param placement location to place disk
     * @param remaining init flip table for these whether they're mover, enemy, or both
     * @param mover mover squares
     * @param enemy enemy squares
     */
    private void initFlipTable(long placement, long remaining, long mover, long enemy) {
        if (remaining==0) {
            final int index = calcIndex(mover, enemy);
            final long flips = BitBoardUtils.fillULDR(mover, enemy, placement);
            flipTable[index] = flips;
        }
        else {
            long low = Long.lowestOneBit(remaining);
            remaining &=~low;
            initFlipTable(placement, remaining, mover, enemy);
            initFlipTable(placement, remaining, mover | low, enemy);
            initFlipTable(placement, remaining, mover, enemy | low);
        }
    }

    private static long diagonal(int sq, int row, int col) {
        long mask = 1L << sq;
        for (int r = row, c= col; r>=0 && c>=0; r--, c--) {
            mask |= bit(r, c);
        }
        for (int r=row, c=col; r<8 && c<8; r++, c++) {
            mask |= bit(r, c);
        }
        return mask;
    }

    public long flips(long mover, long enemy) {
        final int index = calcIndex(mover, enemy);
        return flipTable[index];
    }

    private int calcIndex(long mover, long enemy) {
        final int moverIndex = bbIndex(mover);
        final int enemyIndex = bbIndex(enemy);
        final int index = (moverIndex << length) + enemyIndex;
        assert index<65536;
        return index;
    }

    /**
     * From a bitBoard, calculate the moverRow / enemyRow index into the lookup table
     *
     * @param bb board containing mover/enemy disks
     * @return moverRow/ enemyRow
     */
    private int bbIndex(long bb) {
        final int index = (int) (((bb & diagonal) * BitBoardUtils.HFile) >>> shift);
//            assert index < 256;
//            assert index >= 0;
        return index;
    }
}
