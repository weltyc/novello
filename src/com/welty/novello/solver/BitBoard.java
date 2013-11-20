package com.welty.novello.solver;

import com.orbanova.common.misc.Require;
import org.jetbrains.annotations.Nullable;

import static java.lang.Long.bitCount;

/**
 * Bitboard representation of an Othello board
 */
public class BitBoard implements Comparable<BitBoard> {
    static String header = "  A B C D E F G H  \n";
    public final long black;
    public final long white;
    public final boolean blackToMove;

    /**
     * Construct with given bitBoard representation
     *
     * @param black       black disk bitboard
     * @param white       white disk bitboard
     * @param blackToMove true if black's move; false if white's
     */
    public BitBoard(long black, long white, boolean blackToMove) {
        this.black = black;
        this.white = white;
        this.blackToMove = blackToMove;
        validate();
    }

    /**
     * This parses the text representation of the board's contents and stores it in the black and white bitboards.
     * <p/>
     * The first character of the boardString is the color of the disk representing A1; the second is A2, the 9th is B1.
     * Allowed characters are:
     * <UL>
     * <li>'0', 'o' or 'O' for a white disk</li>
     * <li>'*', 'x' or 'X' for a black disk</li>
     * <li>'.', '-', or '_' for an empty square</li>
     * <li>' ' which is ignored; it can be used to separate rows on the board for better legibility</li>
     * </UL>
     * Any other character triggers an exception
     * <p/>
     * The high bit of black is set if A1 contains a black disk; the second highest bit is set if A2 contains a black disk, and
     * and so on. white is set the same way based on squares containing white disks.
     */
    public BitBoard(String boardString, boolean blackToMove) {
        boardString = boardString.replace(" ", "");
        Require.eq(boardString.length(), "board text length", 64);

        long blackDisks = 0;
        long whiteDisks = 0;
        for (int i = 0; i < 64; i++) {
            blackDisks *= 2;
            whiteDisks *= 2;
            char c = boardString.charAt(i);
            switch (c) {
                case '0':
                case 'O':
                case 'o':
                    whiteDisks++;
                    break;
                case '*':
                case 'X':
                case 'x':
                    blackDisks++;
                    break;
                case '.':
                case '_':
                case '-':
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected board character '" + c + "'");
            }
        }

        validate();
        this.black = blackDisks;
        this.white = whiteDisks;
        this.blackToMove = blackToMove;
    }

    private void validate() {
        if ((black & white) != 0) {
            throw new IllegalStateException("Internal error.");
        }
    }

    /**
     * Play a move.
     * <p/>
     * This is designed for ease of use and error reporting rather than optimal efficiency. For optimal
     * efficiency call square.calcFlips() directly without creating objects.
     *
     * @param sq index of square to play
     * @return a new BitBoard containing the board position after the move
     */
    public BitBoard play(int sq) {
        return play(Square.of(sq));
    }

    /**
     * Play a move.
     * <p/>
     * This is designed for ease of use and error reporting rather than optimal efficiency. For optimal
     * efficiency call square.calcFlips() directly without creating objects.
     *
     * @param square move to make, or null to pass
     * @return a new BitBoard containing the board position after the move
     */
    public BitBoard play(@Nullable Square square) {
        if (square == null) {
            return pass();
        }

        if (0 != (square.placement() & (black | white))) {
            throw new IllegalArgumentException("Illegal move - already filled: " + square);
        }
        long mover = mover();
        long enemy = enemy();
        final long flip = square.calcFlips(mover, enemy);
        if (flip == 0) {
            throw new IllegalArgumentException("Illegal move - doesn't flip: " + square);
        }
        mover = mover | flip | square.placement();
        enemy = enemy & ~flip;
        if (blackToMove) {
            return new BitBoard(mover, enemy, !blackToMove);
        } else {
            return new BitBoard(enemy, mover, !blackToMove);
        }
    }

    public long mover() {
        return blackToMove ? black : white;
    }

    public long enemy() {
        return blackToMove ? white : black;
    }


    /**
     * @return value of the game to the mover.
     */
    public int solve() {
        return new Solver().solve(mover(), enemy());
    }

    /**
     * @return Text of the contents of the board, without player-to-move.
     */
    String boardString() {
        StringBuilder sb = new StringBuilder();
        long b = black;
        long w = white;
        while (sb.length() < 64) {
            if (b < 0) {
                sb.append('*');
            } else {
                sb.append(w < 0 ? 'O' : '-');
            }
            b <<= 1;
            w <<= 1;
        }
        return sb.toString();
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        final String boardString = boardString();
        sb.append(header);
        for (int row = 0; row < 8; row++) {
            final int rowDisplayIndex = row + 1;
            sb.append(rowDisplayIndex).append(' ');
            String rowString = boardString.substring(row * 8, row * 8 + 8);
            for (char c : rowString.toCharArray()) {
                sb.append(c).append(' ');
            }
            sb.append(rowDisplayIndex).append('\n');
        }
        sb.append(header);
        sb.append("\nWhite: ").append(bitCount(white))
                .append("  Black: ").append(bitCount(black))
                .append("  Empty: ").append(bitCount(~(white | black)))
                .append('\n');
        sb.append(blackToMove ? "Black" : "White").append(" to move\n");
        return sb.toString();
    }

    /**
     * @return Empty disks bitboard
     */
    public long empty() {
        return ~(black | white);
    }

    /**
     * @return true if there is at least one legal move from this position
     */
    public boolean hasLegalMove() {
        return legalMoves() != 0;
    }

    /**
     * @return bitboard of legal moves from this position
     */
    public long legalMoves() {
        return BitBoardUtils.calcMoves(mover(), enemy());
    }

    /**
     * @return bitboard that occurs after a pass (color changes but not disks).
     */
    public BitBoard pass() {
        return new BitBoard(black, white, !blackToMove);
    }

    /**
     * @return black disks - white disks
     */
    public int netDisks() {
        return Long.bitCount(black) - Long.bitCount(white);
    }

    /**
     * Perform one of the 8 reflections of the position and return the result.
     * <p/>
     * See {@link BitBoardUtils#reflection} for a description of reflection.
     *
     * @param r index of reflection, 0..7
     * @return reflected position.
     */
    public BitBoard reflection(int r) {
        long b = BitBoardUtils.reflection(black, r);
        long w = BitBoardUtils.reflection(white, r);
        return new BitBoard(b, w, blackToMove);
    }

    /**
     * Calculate the minimal reflection of this.
     *
     * Of the 8 reflections of this BitBoard, the minimal one is the one
     * that compares smallest using compareTo().
     *
     * @return minimal reflection of the bitboard.
     */
    public BitBoard minimalReflection() {
        BitBoard minimal = this;
        for (int r = 1; r < 8; r++) {
            BitBoard reflection = reflection(r);
            if (reflection.compareTo(minimal) < 0) {
                minimal = reflection;
            }
        }
        return minimal;
    }

    @Override public int compareTo(BitBoard o) {
        if (black!=o.black) {
            return Long.compare(black, o.black);
        }
        if (white!=o.white) {
            return Long.compare(white, o.white);
        }
        return Boolean.compare(blackToMove, o.blackToMove);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BitBoard bitBoard = (BitBoard) o;

        if (black != bitBoard.black) return false;
        if (blackToMove != bitBoard.blackToMove) return false;
        if (white != bitBoard.white) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (black ^ (black >>> 32));
        result = 31 * result + (int) (white ^ (white >>> 32));
        result = 31 * result + (blackToMove ? 1 : 0);
        return result;
    }

    /**
     * @return number of empty disks
     */
    public int nEmpty() {
        return bitCount(empty());
    }

    /**
     * @return bitBoard of legal moves
     */
    public long calcMoves() {
        return BitBoardUtils.calcMoves(mover(), enemy());
    }

    public static BitBoard ofMover(long mover, long enemy, boolean blackToMove) {
        if (blackToMove) {
            return new BitBoard(mover, enemy, blackToMove);
        }
        else {
            return new BitBoard(enemy, mover, blackToMove);
        }
    }
}
