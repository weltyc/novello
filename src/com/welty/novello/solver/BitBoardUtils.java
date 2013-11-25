package com.welty.novello.solver;

/**
 */
public class BitBoardUtils {

    //
    // Sort order for checking squares
    //
    public static final long AFile = 0x8080808080808080L;
    public static final long BFile = AFile >>> 1;
    public static final long CFile = AFile >>> 2;
    public static final long DFile = AFile >>> 3;
    public static final long EFile = AFile >>> 4;
    public static final long FFile = AFile >>> 5;
    public static final long GFile = AFile >>> 6;
    public static final long HFile = AFile >>> 7;
    private static final long FilesAH = AFile | HFile;

    private static final long Rank1 = 0xFF;
    private static final long Rank8 = 0xFFL << 56;
    private static final long Ranks18 = Rank1 | Rank8;

    public static final long CORNERS = 0x8100000000000081L;
    static final long C_SQUARES = 0x4281000000008142L;
    public static final long X_SQUARES = 0x0042000000004200L;

    private static final long EDGES = FilesAH | Ranks18;
    private static final long CENTER_36 = ~EDGES;
    public static final long CENTER_4 = 0x0000001818000000L;



    /**
     * Column number of square.
     * <p/>
     * Note that column number 0 is usually displayed on the right hand side of the screen (H-file)
     *
     * @param sq square
     * @return Column number
     */
    static int col(int sq) {
        return sq & 7;
    }

    /**
     * Row number of square.
     * <p/>
     * Note that row number 0 is usually displayed at the top of the screen as row number 1.
     * <p/>
     * Unchecked Precondition: square is legal (0..63).
     *
     * @param sq square
     * @return Row number
     */
    static int row(int sq) {
        return sq >> 3;
    }

    /**
     * @param mask bitboard
     * @param sq   bit index
     * @return true if bit number sq is set in the mask, false if it is clear.
     */
    static boolean isBitSet(long mask, int sq) {
        return ((mask >> sq) & 1) == 1;
    }

    /**
     * @param mask bitboard
     * @param sq   bit index
     * @return true if bit number sq is clear in the mask, false if it is set.
     */
    static boolean isBitClear(long mask, int sq) {
        return ((mask >> sq) & 1) == 0;
    }

    /**
     * Get the bit of the mask
     *
     * @param mask mask to check
     * @param sq   bit index
     * @return 1 if bit is set, 0 if bit is clear
     */
    public static long getBit(long mask, int sq) {
        return ((mask >> sq) & 1);
    }

    /**
     * Get the bit of the mask
     *
     * @param mask mask to check
     * @param sq   bit index
     * @return 1 if bit is set, 0 if bit is clear
     */
    public static int getBitAsInt(long mask, int sq) {
        return (int) getBit(mask, sq);
    }

    /**
     * smear bits up.
     * <p/>
     * The bits of g are smeared, but only along set bits of p.
     */
    static long fillUp(long g, long p) {
        g |= p & (g << 8);
        p &= (p << 8);
        g |= p & (g << 16);
        p &= (p << 16);
        g |= p & (g << 32);
        return g;
    }

    /**
     * smear bits down.
     * <p/>
     * The bits of g are smeared, but only along set bits of p.
     */
    static long fillDown(long g, long p) {
        g |= p & (g >>> 8);
        p &= (p >>> 8);
        g |= p & (g >>> 16);
        p &= (p >>> 16);
        g |= p & (g >>> 32);
        return g;
    }

    /**
     * smear bits left
     * <p/>
     * The bits of g are smeared, but only along set bits of p.
     */
    static long fillLeft(long g, long p) {
        p &= ~HFile;
        g |= p & (g << 1);
        p &= (p << 1);
        g |= p & (g << 2);
        p &= (p << 2);
        g |= p & (g << 4);
        return g;
    }

    /**
     * smear bits right.
     * <p/>
     * The bits of g are smeared, but only along set bits of p.
     * <p/>
     * All fill functions use the <a href="http://chessprogramming.wikispaces.com/Kogge-Stone+Algorithm">Kogge-Stone algorithm</a>
     */
    static long fillRight(long g, long p) {
        p &= ~AFile;
        g |= p & (g >>> 1);
        p &= (p >>> 1);
        g |= p & (g >>> 2);
        p &= (p >>> 2);
        g |= p & (g >>> 4);
        return g;
    }

    /**
     * smear bits up/left
     * <p/>
     * The bits of g are smeared, but only along set bits of p.
     */
    static long fillUpLeft(long g, long p) {
        p &= ~HFile;
        g |= p & (g << 9);
        p &= (p << 9);
        g |= p & (g << 18);
        p &= (p << 18);
        g |= p & (g << 36);
        return g;
    }

    /**
     * smear bits down/right
     * <p/>
     * The bits of g are smeared, but only along set bits of p.
     */
    static long fillDownRight(long g, long p) {
        p &= ~AFile;
        g |= p & (g >>> 9);
        p &= (p >>> 9);
        g |= p & (g >>> 18);
        p &= (p >>> 18);
        g |= p & (g >>> 36);
        return g;
    }

    /**
     * smear bits up/right
     * <p/>
     * The bits of g are smeared, but only along set bits of p.
     */
    private static long fillUpRight(long g, long p) {
        p &= ~AFile;
        g |= p & (g << 7);
        p &= (p << 7);
        g |= p & (g << 14);
        p &= (p << 14);
        g |= p & (g << 28);
        return g;
    }

    /**
     * smear bits down/left
     * <p/>
     * The bits of g are smeared, but only along set bits of p.
     */
    private static long fillDownLeft(long g, long p) {
        p &= ~HFile;
        g |= p & (g >>> 7);
        p &= (p >>> 7);
        g |= p & (g >>> 14);
        p &= (p >>> 14);
        g |= p & (g >>> 28);
        return g;
    }

    /**
     * Reflect the bitBoard about the A1..H8 diagonal.
     * <p/>
     * A8, at bit 7, is interchanged with H1, at bit 56.
     *
     * @param bb original bitboard
     * @return diagonal reflection of the bitBoard
     */
    public static long reflectDiagonally(long bb) {
        bb = flipDiagonalBlock(bb, 0x5500550055005500L, 0x00AA00AA00AA00AAL, 7);         // flip 1x1 blocks within 2x2 blocks
        bb = flipDiagonalBlock(bb, 0x3333000033330000L, 0x0000CCCC0000CCCCL, 14);         // flip 2x2 blocks within 4x4 blocks
        bb = flipDiagonalBlock(bb, 0x0F0F0F0F00000000L, 0x00000000F0F0F0F0L, 28);         // flip 4x4 blocks within 8x8 blocks
        return bb;
    }

    /**
     * Calculate a bitboard reflection.
     * <p/>
     * Reflections rearrange the bitboard by rotating or flipping it.
     * <p/>
     * Which reflection is chosen is determined by index, which is a bit mask from 0...7. 0 is the identity transformation;
     * the result is the same as the input bitboard.
     * <p/>
     * Index values:
     * <br/> 1 = reverse
     * <br/> 2 = reverse bytes
     * <br/> 4 = diagonal
     * <p/>
     * In geometric terms,
     * <br/> 0 leaves the bitboard unchanged
     * <br/> 6 rotates right 90 degrees
     * <br/> 7 rotates left 90 degrees
     * <br/> 1 rotates 180 degrees
     * <p/>
     * <br/> 2 flips the board vertically
     * <br/> 3 flips the board horizontally
     * <br/> 4 flips the board diagonally; A8 and H1 switch places
     * <br/> 5 flips the board diagonally; A1 and H8 switch places.
     *
     * @param bb    bitboard to reflect
     * @param index reflection index from 0..7
     * @return reflected bitboard
     */
    public static long reflection(long bb, int index) {
        if (isBitSet(index, 0)) {
            bb = Long.reverse(bb);
        }
        if (isBitSet(index, 1)) {
            bb = Long.reverseBytes(bb);
        }
        if (isBitSet(index, 2)) {
            bb = reflectDiagonally(bb);
        }
        return bb;
    }

    private static long flipDiagonalBlock(long rows, long maskR, long maskL, int move) {
        final long tempL = maskL & rows;
        final long tempR = maskR & rows;
        rows ^= (tempL | tempR);
        rows |= (tempL << move) | (tempR >>> move);

        return rows;
    }

    /**
     * Bitboard containing a single set bit
     *
     * @param row row of square to set
     * @param col col of square to set
     * @return bitboard
     */
    public static long bit(int row, int col) {
        return 1L << square(row, col);
    }

    /**
     * Calc square index from row and col index
     *
     * @param row index of row
     * @param col index of col
     * @return square index
     */
    private static int square(int row, int col) {
        return row * 8 + col;
    }

    static long fillLR(long mover, long enemy, long placement) {
        return (fillLeft(placement, enemy) & fillRight(mover, enemy)) | (fillRight(placement, enemy) & fillLeft(mover, enemy));
    }

    static long fillURDL(long mover, long enemy, long placement) {
        return (fillDownLeft(placement, enemy) & fillUpRight(mover, enemy)) | (fillUpRight(placement, enemy) & fillDownLeft(mover, enemy));
    }

    static long fillULDR(long mover, long enemy, long placement) {
        return (fillUpLeft(placement, enemy) & fillDownRight(mover, enemy)) | (fillDownRight(placement, enemy) & fillUpLeft(mover, enemy));
    }

    private static final long notEastEdge = 0xFEFEFEFEFEFEFEFEL;
    private static final long notWestEdge = 0x7F7F7F7F7F7F7F7FL;

    /**
     * Calculate mobilities (legal moves for the mover)
     *
     * @return mobility bitboard
     */
    public static long calcMoves(long mover, long enemy) {
        //	a direction bit is set if we have seen a mover followed by an unbroken string of enemy squares

        long south = (mover << 8);
        long north = (mover >>> 8);

        final long moverW = mover & notWestEdge;
        final long moverE = mover & notEastEdge;

        long west = (moverW << 1);
        long east = (moverE >>> 1);

        long northwest = (moverW >>> 7);
        long northeast = (moverE >>> 9);
        long southwest = (moverW << 9);
        long southeast = (moverE << 7);

        final long enemyW = enemy & notWestEdge;
        final long enemyE = enemy & notEastEdge;

        long moves = 0;
        final long empty = ~(mover | enemy);

        for (int i = 0; i < 6; i++) {
            south = (south & enemy) << 8;
            north = (north & enemy) >>> 8;
            west = ((west & enemyW) << 1);
            east = ((east & enemyE) >>> 1);

            northwest = ((northwest & enemyW) >>> 7);
            northeast = ((northeast & enemyE) >>> 9);
            southwest = ((southwest & enemyW) << 9);
            southeast = ((southeast & enemyE) << 7);
            moves |= ((north | south | west | east | northwest | northeast | southwest | southeast) & empty);
        }
        return moves;
    }

    /**
     * Convert square index to text.
     *
     * This uses Novello conventions; the row and col increase as the text chars decrease.
     *
     * @param sq index of square. A1 = 63, H1 = 56.
     * @return text, for instance "D5"
     */
    public static String sqToText(int sq) {
        final int col = col(sq);
        final int row = row(sq);
        return rowColToText(col, row);
    }

    /**
     * Convert row and col indices to text
     *
     * This uses Novello conventions; the row and col increase as the text chars decrease.
     *
     * @param col index of column. "H"=0, "A"=7.
     * @param row index of row. "8" = 0, "1" = 7
     * @return text, for instance "D5"
     */
    private static String rowColToText(int col, int row) {
        final char colChar = (char) ('H' - col);
        final char rowChar = (char) ('8' - row);
        return "" + colChar + rowChar;
    }

    /**
     * Number of empty disks
     *
     * @param mover mover disk bitboard
     * @param enemy enemy disk bitboard
     * @return number of empty disks
     */
    public static int nEmpty(long mover, long enemy) {
        return Long.bitCount(~(mover|enemy));
    }

    /**
     * Calculate potential mobilities.
     *
     * A potential mobility is a disk on the border of the playing field: there's at least one direction where
     * it's adjacent to an enemy square and not on the edge of the board in that direction.
     *
     * @return location of potMobs.
     */
    public static long potMobs(long player, long empty) {
        final long upDown =  ((empty>>>8)|(empty<<8))&(player&~Ranks18);
        final long leftRight = ((empty>>>1 | empty<<1))&(player&~FilesAH);
        final long diagonal = ((empty>>7)|(empty>>9)|(empty<<7)|(empty<<9))&(player&CENTER_36);
        return upDown | leftRight | diagonal;
    }
}
