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

import com.orbanova.common.misc.Utils;
import com.welty.novello.core.BitBoardUtils;

public class Corner2x4Term extends Term {
    final boolean isRow;
    final boolean isLeft;
    final int row;

    private static final Feature myFeature = new Corner2x4Feature();
    private static final int[] base3FromBase2Rev4 = new int[16];
    static final Corner2x4Term[] terms = {
            new Corner2x4Term(0),
            new Corner2x4Term(1),
            new Corner2x4Term(2),
            new Corner2x4Term(3),
            new Corner2x4Term(4),
            new Corner2x4Term(5),
            new Corner2x4Term(6),
            new Corner2x4Term(7),
    };

    static {
        for (int i = 0; i < 16; i++) {
            final int revI = Integer.reverse(i) >>> 28;
            base3FromBase2Rev4[i] = Base3.base2ToBase3(revI, 0);
        }
    }

    private final int position;

    public Corner2x4Term(int position) {
        super(myFeature);
        this.position = position;
        isLeft = !Utils.isOdd(position);
        row = (position & 2) == 0 ? 0 : 7;
        isRow = (position & 4) == 0;
    }

    @Override public int instance(long mover, long enemy, long moverMoves, long enemyMoves) {
        final int mover0;
        final int mover1;
        final int enemy0;
        final int enemy1;

        if (isRow) {
            mover0 = BitBoardUtils.extractRow(mover, row);
            mover1 = BitBoardUtils.extractRow(mover, row ^ 1);
            enemy0 = BitBoardUtils.extractRow(enemy, row);
            enemy1 = BitBoardUtils.extractRow(enemy, row ^ 1);
        } else {
            mover0 = BitBoardUtils.extractCol(mover, row);
            mover1 = BitBoardUtils.extractCol(mover, row ^ 1);
            enemy0 = BitBoardUtils.extractCol(enemy, row);
            enemy1 = BitBoardUtils.extractCol(enemy, row ^ 1);
        }

        if (isLeft) {
            return leftInstance(mover0, enemy0, mover1, enemy1);
        }
        else {
            return rightInstance(mover0, enemy0, mover1, enemy1);
        }
    }

    @Override String oridGen() {
        StringBuilder sb = new StringBuilder();
        sb.append("Corner2x4Term.").append(isLeft ? "leftInstance(" : "rightInstance(");
        final String type = isRow ? "Row"  : "Col";
        final String row0 = type + row;
        final String row1 = type + (row^1);
        sb.append("mover"+row0+", enemy"+row0+", mover"+row1+", enemy"+row1);
        sb.append(")");
        return sb.toString();
    }

    private static int rightBase3(int mover, int enemy) {
        return Base3.base2ToBase3(mover & 0xF, enemy & 0xF);
    }

    static int rightInstance(int mover0, int enemy0, int mover1, int enemy1) {
        return rightBase3(mover0, enemy0) + 81* rightBase3(mover1, enemy1);
    }

    static int leftInstance(int mover0, int enemy0, int mover1, int enemy1) {
        return leftBase3(mover0, enemy0) + 81* leftBase3(mover1, enemy1);
    }

    private static int leftBase3(int mover, int enemy) {
        return leftBase3(mover) + 2 * leftBase3(enemy);
    }

    private static int leftBase3(int disks) {
        return base3FromBase2Rev4[ (disks >>> 4) & 0xF];
    }
}
