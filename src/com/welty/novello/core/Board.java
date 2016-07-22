/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.core;

import com.orbanova.common.misc.Require;
import com.welty.othello.gdk.COsBoard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.Long.bitCount;

/**
 * Bitboard representation of an Othello board. Immutable.
 * <p/>
 * This includes the disks on the board and the player to move.
 * <p/>
 * For a stripped down representation containing only the mover and enemy disks, see {@link Me}.
 */
public class Board implements Comparable<Board> {
    private static final String header = "  A B C D E F G H  \n";
    public static final Board START_BOARD = new Board(0x0000000810000000L, 0x0000001008000000L, true);
    public static final Board ALTERNATE_START_BOARD = new Board(0x0000001010000000L, 0x0000000808000000L, true);
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
    public Board(long black, long white, boolean blackToMove) {
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
    public Board(String boardString, boolean blackToMove) {
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

    public Board(MinimalReflection mr) {
        this(mr.mover, mr.enemy, true);
    }

    /**
     * Create a board from text containing both the board position and the player-to-move.
     *
     * @param positionString 64 characters representing the board, ("oO0" for white, "xX*" for black, ".-_" for empty)
     * followed by one character representing the player-to-move. Whitespace characters may occur anywhere in the
     * positionString and are ignored.
     * @return the board
     */
    public static Board of(String positionString) {
        final String squished = positionString.replaceAll("\\s+", "");
        Require.eq(squished.length(), "position string length", 65);
        char moverChar = squished.charAt(64);
        final boolean blackToMove;
        switch(moverChar) {
            case 'x':
            case '*':
            case 'X':
                blackToMove = true;
                break;
            case 'o':
            case 'O':
            case '0':
                blackToMove = false;
                break;
            default:
                throw new RuntimeException("Format error: '" + moverChar + "' is not a legal player-to-move character in board text " + positionString);
        }
        return new Board(squished.substring(0, 64), blackToMove);
    }

    public static Board of(COsBoard board) {
        final COsBoard.GetTextResult text = board.getText();
        return new Board(text.getText(), text.isBlackMove());
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
    public Board play(String moveText) {
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
    public Board play(int sq) {
        return play(Square.of(sq));
    }

    /**
     * Play a move, if sq >= 0, or pass if sq <0
     *
     * @param sq square index
     * @return new Position
     */
    public Board playOrPass(int sq) {
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
    Board play(@Nullable Square square) {
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
            return new Board(mover, enemy, !blackToMove);
        } else {
            return new Board(enemy, mover, !blackToMove);
        }
    }

    public long mover() {
        return blackToMove ? black : white;
    }

    public long enemy() {
        return blackToMove ? white : black;
    }


    /**
     * Get the text of the contents of the board, with spaces between each row of pieces.
     *
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
    public Board pass() {
        return new Board(black, white, !blackToMove);
    }

    /**
     * Net score, in disks, to black.
     * <p/>
     * Winner gets empties if {@link com.welty.novello.core.BitBoardUtils#WINNER_GETS_EMPTIES} is true.
     *
     * @return black disks - white disks
     */
    public int terminalScoreToBlack() {
        return BitBoardUtils.terminalScore(black, white);
    }

    /**
     * Net score, in disks, to black.
     * <p/>
     * Winner gets empties if {@link com.welty.novello.core.BitBoardUtils#WINNER_GETS_EMPTIES} is true.
     *
     * @return black disks - white disks
     */
    public int terminalScoreToMover() {
        final int score = BitBoardUtils.terminalScore(black, white);
        return blackToMove ? score : -score;
    }

    /**
     *
     */

    /**
     * Perform one of the 8 reflections of the position and return the result.
     * <p/>
     * See {@link BitBoardUtils#reflection} for a description of reflection.
     *
     * @param r index of reflection, 0..7
     * @return reflected position.
     */
    public Board reflection(int r) {
        long b = BitBoardUtils.reflection(black, r);
        long w = BitBoardUtils.reflection(white, r);
        return new Board(b, w, blackToMove);
    }

    /**
     * Calculate the minimal reflection of this.
     * <p/>
     * Of the 8 reflections of this Position, the minimal one is the one
     * that compares smallest using compareTo().
     *
     * @return minimal reflection of the bitboard.
     */
    public MinimalReflection minimalReflection() {
        return new MinimalReflection(mover(), enemy());
    }

    @Override public int compareTo(@NotNull Board o) {
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

        Board board = (Board) o;

        if (black != board.black) return false;
        if (blackToMove != board.blackToMove) return false;
        if (white != board.white) return false;

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

    public static Board ofMover(long mover, long enemy, boolean blackToMove) {
        if (blackToMove) {
            return new Board(mover, enemy, blackToMove);
        } else {
            return new Board(enemy, mover, blackToMove);
        }
    }

    /**
     * @return 0 if mover has a move, else 1 if enemy has a move, else 2
     */
    public int calcPass() {
        if (calcMoves() != 0) {
            return 0;
        }
        if (enemyMoves() != 0) {
            return 1;
        }
        return 2;
    }

    public MinimalReflection toMr() {
        return new MinimalReflection(mover(), enemy());
    }
}
