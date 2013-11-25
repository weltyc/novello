package com.welty.novello.solver;

import static com.welty.novello.solver.BitBoardUtils.*;

/**
 * Can update bitboards when the mover places a disk in the square.
 * <p/>
 * All methods assume that the bitboard is currently empty in its square
 */
public class Square {
    private static final Square[] squares = new Square[64];
    static {
        for (int i = 0; i < 64; i++) {
            squares[i] = new Square(i);
        }
    }

    public static final Square H7 = squares[8];

    // bitboard parity regions
    private static final long BOTTOM_RIGHT = 0x0F0F0F0F;
    private static final long BOTTOM_LEFT = BOTTOM_RIGHT << 4;
    private static final long TOP_RIGHT = BOTTOM_RIGHT << 32;
    private static final long TOP_LEFT = TOP_RIGHT << 4;


    final int sq;
    /**
     * BitMask containing all squares whose parity will be calculated along with this square
     */
    final long parityRegion;

    /**
     * ULDR flip function
     */
    private final FlipperULDR flipperULDR;

    /**
     * URDL flip function
     */
    private final FlipperURDL flipperURDL;

    /**
     * Get the Square from the square index
     *
     * @param sq square index
     * @return the Square
     */
    public static Square of(int sq) {
        return squares[sq];
    }

    /**
     * Calculate the squares on the board that will be flipped
     * <p/>
     * If this returns 0, the move is not legal.
     *
     * @return a bitboard of disks that will be flipped.
     */
    public long calcFlips(long mover, long enemy) {
        long flips = KindergartenEastWest.flips(sq, mover, enemy) | KindergartenUpDown.flips(sq, mover, enemy)
                | flipperURDL.flips(mover, enemy) | flipperULDR.flips(mover, enemy);
        return flips;
    }

    /**
     * @return a bitboard containing only one set bit - this Square's.
     */
    long placement() {
        return 1L << sq;
    }

    Square(int sq) {
        this.sq = sq;
        if (isBitSet(TOP_LEFT, sq)) {
            parityRegion = TOP_LEFT;
        } else if (isBitSet(TOP_RIGHT, sq)) {
            parityRegion = TOP_RIGHT;
        } else if (isBitSet(BOTTOM_LEFT, sq)) {
            parityRegion = BOTTOM_LEFT;
        } else {
            parityRegion = BOTTOM_RIGHT;
        }
        flipperULDR = new FlipperULDR(sq);
        flipperURDL = new FlipperURDL(sq);
    }

    @Override public String toString() {
        final int sq = this.sq;
        return sqToText(sq);
    }
}

