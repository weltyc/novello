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

import static com.welty.novello.core.BitBoardUtils.*;

/**
 */
class KindergartenUpDown {

    private static int index(int row, int moverRow, int enemyRow) {
        return (row * 65536 + moverRow * 256 + enemyRow);
    }

    private static int indexFromBitBoard(int sq, long mover, long enemy) {
        final int col = col(sq);
        final int row = row(sq);
        return index(row, extractCol(mover, col), extractCol(enemy, col));
    }

    private static final long[] flipTable = createFlipTable();

    private static long[] createFlipTable() {
        long[] flipTable = new long[8 * 256 * 256];
        // flipTable[col*65536 + mover row * 256 + enemy row] = flips
        for (int moverRow = 0; moverRow < 256; moverRow++) {
            for (int enemyRow = 0; enemyRow < 256; enemyRow++) {
                if ((moverRow & enemyRow) == 0) {
                    final long mover = BitBoardUtils.reflectDiagonally(moverRow);
                    final long enemy = BitBoardUtils.reflectDiagonally(enemyRow);
                    final long empty = ~(mover | enemy);
                    for (int sq = 0; sq < 64; sq += 8) {
                        if (BitBoardUtils.isBitSet(empty, sq)) {
                            final long placement = 1L << sq;
                            final long flips = (fillUp(placement, enemy) & fillDown(mover, enemy)) | (fillDown(placement, enemy) & fillUp(mover, enemy));
                            final int index = indexFromBitBoard(sq, mover, enemy);
                            flipTable[index] = flips;
                        }
                    }
                }
            }
        }
        return flipTable;
    }

    /**
     * @param sq    square index to place disk
     * @param mover mover disks
     * @param enemy enemy disks
     * @return squares that would be flipped in the east/west direction if a mover was placed at sq
     */
    static long flips(int sq, long mover, long enemy) {
        final int index = indexFromBitBoard(sq, mover, enemy);
        final int col = col(sq);
        final long flips = flipTable[index] << col;
        return flips;
    }
}
