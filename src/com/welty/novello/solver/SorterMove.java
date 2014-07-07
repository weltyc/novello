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

package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;

/**
 *  Representation of an othello move, for use in move sorting.
 *
 *  Some information about the move (e.g. flip bitboard) is stored to
 *  save time compared to recalculating it.
 */
class SorterMove {
    int sq;
    int score;
    long flips;
    long enemyMoves;
    ListOfEmpties.Node node;

    @Override public String toString() {
        return "Move" + BitBoardUtils.sqToText(sq) + ", enemy mobs = " + Long.bitCount(enemyMoves);
    }
}
