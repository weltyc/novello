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

package com.welty.novello.eval;

import com.welty.novello.core.BitBoardUtils;

import static com.welty.novello.core.BitBoardUtils.getBitAsInt;

public class CornerTerm2 extends Term {
    private final int sq;

    @SuppressWarnings("OctalInteger")
    public static final CornerTerm2[] terms = {
            new CornerTerm2(000), new CornerTerm2(007), new CornerTerm2(070), new CornerTerm2(077)
    };

    public CornerTerm2(int sq) {
        super(Features.corner2Feature);
        this.sq = sq;
    }

    /**
     * Instance =
     * 4 mover occupies corner
     * 5 enemy occupies corner
     * <p/>
     * if corner is empty,
     * 1 mover has access
     * 2 enemy has access
     * 3 both players have access
     * <p/>
     * if nobody has access,
     * 0 = empty x-square
     * 6 = mover on x-square,
     * 7 = enemy on x-square
     *
     * @return the instance for this term
     */
    @Override
    public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        return orid(mover, enemy, moverMoves, enemyMoves, sq);
    }

    static int orid(long mover, long enemy, long moverMoves, long enemyMoves, int sq) {
        final int cornerOccupier = getBitAsInt(mover, sq) + 2 * getBitAsInt(enemy, sq);
        if (cornerOccupier > 0) {
            return cornerOccupier + 3;
        }
        final int cornerMobility = getBitAsInt(moverMoves, sq) + 2 * getBitAsInt(enemyMoves, sq);
        if (cornerMobility > 0) {
            return cornerMobility;
        } else {
            @SuppressWarnings("OctalInteger")
            final int xSq = sq ^ 011;
            final int xSquareOccupier = BitBoardUtils.getBitAsInt(mover, xSq) + 2 * BitBoardUtils.getBitAsInt(enemy, xSq);
            if (xSquareOccupier > 0) {
                return xSquareOccupier + 5;
            } else {
                return 0;
            }
        }
    }


    @Override String oridGen() {
        return "CornerTerm2.orid(mover, enemy, moverMoves, enemyMoves, " + sq + ")";
    }
}
