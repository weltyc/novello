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
import com.welty.novello.core.KindergartenEastWest;
import com.welty.novello.eval.Base3;

import static com.welty.novello.core.BitBoardUtils.*;

/**
 * Determines whether disks are stable
 */
public class Stable {
    /**
     * Calculate all stable disks on the edge
     * <p/>
     * This uses a lookup table so should be pretty quick.
     *
     * @return bitboard of stable disks on edges
     */
    public static long edgeStable(long mover, long enemy) {
        return row0Stable[Base3.base2ToBase3((int) mover & 0xFF, (int) enemy & 0xFF)]
                | row7Stable[rowInstance(mover, enemy, 56)]
                | col7Stable[colInstance(mover, enemy, 7)]
                | col0Stable[colInstance(mover, enemy, 0)];
    }


    /**
     * Calculate stable disks using Zebra algorithm.
     * <p/>
     * This may not be too quick.
     * <p/>
     * Edge disk stability is detected by edgeStable().
     * <p/>
     * A disk is stable if (and only if) it can't be flipped in any of the four directions.
     * <p/>
     * An interior disk can't be flipped in a direction if
     * 1. it is in a filled row
     * 2. it is adjacent to a stable disk of the same colour
     *
     * @return bitboard of all proven stable disks
     */
    public static long stable(long mover, long enemy) {
        long stable = edgeStable(mover, enemy);

        // find filled rows
        final long empty = ~(mover | enemy);

        long lrStable = ~lrEmpty(empty);
        long udStable = ~udEmpty(empty);
        long uldrStable = ~uldrEmpty(empty);
        long urdlStable = ~urdlEmpty(empty);

        // find disks adjacent to other disks of the same colour
        final long leftOf = ((mover&(mover<<1)) | (enemy & (enemy<<1)))&~HFile;
        final long rightOf = ((mover&(mover>>>1)) | (enemy & (enemy>>>1)))&~AFile;
        final long upOf = ((mover&(mover<<8)) | (enemy & (enemy<<8)));
        final long downOf = ((mover&(mover>>>8)) | (enemy & (enemy>>>8)));
        final long ulOf = ((mover&(mover<<9)) | (enemy & (enemy<<9)))&~HFile;
        final long drOf = ((mover&(mover>>>9)) | (enemy & (enemy>>>9)))&~AFile;
        final long urOf = ((mover&(mover<<7)) | (enemy & (enemy<<7)))&~AFile;
        final long dlOf = ((mover&(mover>>>7)) | (enemy & (enemy>>>7)))&~HFile;

        // iterative - is it adjacent to a stable disk of the same colour?
        for (; ; ) {
            lrStable |= ((stable<<1)&leftOf) | ((stable>>>1)&rightOf);
            udStable |= ((stable<<8)&upOf) | ((stable>>>8)&downOf);
            uldrStable |= ((stable<<9)&ulOf) | ((stable>>>9)&drOf);
            urdlStable |= ((stable<<7)&urOf) | ((stable>>>7)&dlOf);

            long newStable = stable | (lrStable & udStable & uldrStable & urdlStable);
            if (newStable == stable) {
                break;
            }
            stable = newStable;
        }

        return stable;
    }

    private static long lrEmpty(long empty) {
        long lrEmpty = HFile & fillRight(empty, -1);
        lrEmpty |= lrEmpty << 1;
        lrEmpty |= lrEmpty << 2;
        lrEmpty |= lrEmpty << 4;
        return lrEmpty;
    }

    private static long udEmpty(long empty) {
        long udEmpty = empty;
        udEmpty |= (udEmpty >>> 8);
        udEmpty |= (udEmpty >>> 16);
        udEmpty |= (udEmpty >>> 32);
        udEmpty |= (udEmpty << 8);
        udEmpty |= (udEmpty << 16);
        udEmpty |= (udEmpty << 32);
        return udEmpty;
    }

    private static long uldrEmpty(long empty) {
        return BitBoardUtils.fillDownRight(BitBoardUtils.fillUpLeft(empty, -1), -1);
    }

    private static long urdlEmpty(long empty) {
        return BitBoardUtils.fillDownLeft(BitBoardUtils.fillUpRight(empty, -1), -1);
    }

    private static final long[] row0Stable = calcRow0Stable();
    private static final long[] row7Stable = calcRow7Stable(row0Stable);
    private static final long[] col0Stable = calcCol0Stable(row0Stable);
    private static final long[] col7Stable = calcCol7Stable(row0Stable);

    private static long[] calcCol7Stable(long[] row0Stable) {
        final long[] result = new long[row0Stable.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = BitBoardUtils.reflection(row0Stable[i], 5);
        }
        return result;
    }

    private static long[] calcCol0Stable(long[] row0Stable) {
        final long[] result = new long[row0Stable.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = BitBoardUtils.reflection(row0Stable[i], 7);
        }
        return result;
    }


    private static long[] calcRow7Stable(long[] row0Stable) {
        final long[] result = new long[row0Stable.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Long.reverseBytes(row0Stable[i]);
        }
        return result;
    }


    private static long[] calcRow0Stable() {
        final long[] result = new long[6561];
        for (int nFilled = 8; nFilled >= 0; nFilled--) {
            for (int mover = 0; mover < 256; mover++) {
                for (int enemy = 0; enemy < 256; enemy++) {
                    if ((enemy & mover) == 0 && Long.bitCount(enemy | mover) == nFilled) {
                        // figure out if disks are stable by going through each empty square and seeing if
                        // it flips disks.
                        result[Base3.base2ToBase3(mover, enemy)] = stableSearch(mover, enemy, result);
                    }
                }
            }
        }
        return result;
    }

    private static long stableSearch(int mover, int enemy, long[] result) {
        int stables = mover | enemy;

        // check all sub flips
        int empty = 0xFF & ~(enemy | mover);
        while (empty != 0) {
            final int sq = Integer.numberOfTrailingZeros(empty);
            final int loc = Integer.lowestOneBit(empty);
            empty &= ~loc;

            stables &= subStable(mover, enemy, result, sq, loc) & subStable(enemy, mover, result, sq, loc);
        }

        return stables;
    }

    private static long subStable(int mover, int enemy, long[] result, int sq, int loc) {
        final long flips = KindergartenEastWest.flips(sq, mover, enemy);
        final int index = Base3.base2ToBase3((int) (mover | flips | loc) & 0xFF, (int) (enemy & ~flips) & 0xFF);
        return result[index] & ~flips;
    }
}
