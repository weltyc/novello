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

package com.welty.novello.selfplay;

import com.orbanova.common.feed.Feed;
import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Utils;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Board;
import org.jetbrains.annotations.Nullable;

/**
 * Generates a series of BitBoards suitable for self-play start positions.
 *
 * This generates positions with a fixed number of disks, for instance 9.
 * All disks will be placed in the center 16 squares. It generates all such positions once each.
 */
class StartPosGenerator extends Feed<Board> {
    private int i = 0;
    private final int nDisks;
    private final static int I_MAX = 6561 * 6561; // 3^16

    /**
     * Create a start position generator.
     * @param nDisks number of disks. 16 >= nDisks >= 4.
     */
    public StartPosGenerator(int nDisks) {
        Require.geq(nDisks, "nDisks", 4);
        Require.leq(nDisks, "nDisks", 16);
        this.nDisks = nDisks;
    }

    @Nullable @Override public Board next() {
        while (i < I_MAX) {
            final Board board = genBoard(i);
            i++;
            final long empty = board.empty();
            if ((empty & BitBoardUtils.CENTER_4) == 0 && Long.bitCount(~empty) == nDisks) {
                return board;
            }
        }
        return null;
    }

    /**
     * Generate a position with the center 16 squares possibly filled, and the rest empty.
     * Black to move if an even number of disks are filled, white if an odd number are filled.
     * <p/>
     * i is converted to a base-3 representation and the digits of the base-3 representation
     * are used to fill the disks. 0=empty, 1=black, 2=white.
     *
     * @param i index of position
     * @return bitboard
     */
    private Board genBoard(int i) {
        long black = 0;
        long white = 0;
        for (int col = 2; col < 6; col++) {
            for (int row = 2; row < 6; row++) {
                switch (i % 3) {
                    case 1:
                        black |= BitBoardUtils.bit(row, col);
                        break;
                    case 2:
                        white |= BitBoardUtils.bit(row, col);
                        break;
                }
                i=i/3;
            }
        }
        final boolean blackToMove = !Utils.isOdd(Long.bitCount(black | white));
        return new Board(black, white, blackToMove);
    }
}
