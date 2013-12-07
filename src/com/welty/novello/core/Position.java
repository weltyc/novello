package com.welty.novello.core;

import com.orbanova.common.misc.Require;
import org.jetbrains.annotations.Nullable;

import static java.lang.Long.bitCount;

/**
 * Bitboard representation of an Othello board
 */
public class Position implements Comparable<Position> {
    private static final String header = "  A B C D E F G H  \n";
    public static final Position START_POSITION = new Position(0x0000000810000000L, 0x0000001008000000L, true);
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
    public Position(long black, long white, boolean blackToMove) {
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
    public Position(String boardString, boolean blackToMove) {
        boardString = boardString.replaceAll("\\s+", "");
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
                    throw new IllegalArgumentException("Invalid board character '" + c + "'");
            }
        }

        validate();
        this.black = blackDisks;
        this.white = whiteDisks;
        this.blackToMove = blackToMove;
    }

    public static Position of(String positionString) {
        final String squished = positionString.replaceAll("\\s+", "");
        Require.eq(squished.length(), "position string length", 65);
        final boolean blackToMove = "*Xx".contains(squished.substring(64));
        return new Position(squished.substring(0, 64), blackToMove);
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
     * @param moveText text of square to play
     * @return a new Position containing the board position after the move
     */
    public Position play(String moveText) {
        return play(BitBoardUtils.textToSq(moveText));
    }

    /**
     * Play a move.
     * <p/>
     * This is designed for ease of use and error reporting rather than optimal efficiency. For optimal
     * efficiency call square.calcFlips() directly without creating objects.
     *
     * @param sq index of square to play
     * @return a new Position containing the board position after the move
     */
    public Position play(int sq) {
        return play(Square.of(sq));
    }

    /**
     * Play a move, if sq >= 0, or pass if sq <0
     *
     * @param sq square index
     * @return new Position
     */
    public Position playOrPass(int sq) {
        return sq >= 0 ? play(sq) : pass();
    }

    /**
     * Play a move.
     * <p/>
     * This is designed for ease of use and error reporting rather than optimal efficiency. For optimal
     * efficiency call square.calcFlips() directly without creating objects.
     *
     * @param square move to make, or null to pass
     * @return a new Position containing the board position after the move
     */
    Position play(@Nullable Square square) {
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
            return new Position(mover, enemy, !blackToMove);
        } else {
            return new Position(enemy, mover, !blackToMove);
        }
    }

    public long mover() {
        return blackToMove ? black : white;
    }

    public long enemy() {
        return blackToMove ? white : black;
    }


    /**
     * @return Text of the contents of the board, without player-to-move.
     */
    public String boardString() {
        return boardString(" ");
    }

    /**
     * @param lineSeparator separator between lines of the board
     * @return Text of the contents of the board, without player-to-move.
     */
    public String boardString(String lineSeparator) {
        StringBuilder sb = new StringBuilder();
        long b = black;
        long w = white;
        int nWritten = 0;
        while (nWritten < 64) {
            if (b < 0) {
                sb.append('*');
            } else {
                sb.append(w < 0 ? 'O' : '-');
            }
            nWritten++;
            if ((nWritten & 7) == 0 && nWritten < 64) {
                sb.append(lineSeparator);
            }
            b <<= 1;
            w <<= 1;
        }
        return sb.toString();
    }

    /**
     * @return Text of the contents of the board, with player-to-move
     */
    public String positionString() {
        return positionString(" ");
    }

    /**
     * @param lineSeparator separator between lines of the board
     * @return Text of the contents of the board, with player-to-move
     */
    public String positionString(String lineSeparator) {
        return boardString(lineSeparator) + " " + (blackToMove ? '*' : 'O');
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        final String boardString = boardString();
        sb.append(header);
        for (int row = 0; row < 8; row++) {
            final int rowDisplayIndex = row + 1;
            sb.append(rowDisplayIndex).append(' ');
            String rowString = boardString.substring(row * 9, row * 9 + 8);
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
    long legalMoves() {
        return BitBoardUtils.calcMoves(mover(), enemy());
    }

    /**
     * @return bitboard that occurs after a pass (color changes but not disks).
     */
    public Position pass() {
        return new Position(black, white, !blackToMove);
    }

    /**
     * @return black disks - white disks
     */
    public int terminalScore() {
        return BitBoardUtils.terminalScore(black, white);
    }

    /**
     * Perform one of the 8 reflections of the position and return the result.
     * <p/>
     * See {@link BitBoardUtils#reflection} for a description of reflection.
     *
     * @param r index of reflection, 0..7
     * @return reflected position.
     */
    public Position reflection(int r) {
        long b = BitBoardUtils.reflection(black, r);
        long w = BitBoardUtils.reflection(white, r);
        return new Position(b, w, blackToMove);
    }

    /**
     * Calculate the minimal reflection of this.
     * <p/>
     * Of the 8 reflections of this Position, the minimal one is the one
     * that compares smallest using compareTo().
     *
     * @return minimal reflection of the bitboard.
     */
    public Position minimalReflection() {
        Position minimal = this;
        for (int r = 1; r < 8; r++) {
            Position reflection = reflection(r);
            if (reflection.compareTo(minimal) < 0) {
                minimal = reflection;
            }
        }
        return minimal;
    }

    @Override public int compareTo(Position o) {
        if (black != o.black) {
            return Long.compare(black, o.black);
        }
        if (white != o.white) {
            return Long.compare(white, o.white);
        }
        return Boolean.compare(blackToMove, o.blackToMove);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (black != position.black) return false;
        if (blackToMove != position.blackToMove) return false;
        if (white != position.white) return false;

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

    public long enemyMoves() {
        return BitBoardUtils.calcMoves(enemy(), mover());
    }

    public static Position ofMover(long mover, long enemy, boolean blackToMove) {
        if (blackToMove) {
            return new Position(mover, enemy, blackToMove);
        } else {
            return new Position(enemy, mover, blackToMove);
        }
    }
}
