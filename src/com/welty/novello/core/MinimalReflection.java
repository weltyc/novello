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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal reflection of a position. Immutable.
 * <p/>
 * All reflections of a {@link Board} map to the same Mr.
 * This contains only mover and enemy disks, but not color-to-move.
 */
public class MinimalReflection {
    public final long mover;
    public final long enemy;

    public MinimalReflection(long mover, long enemy) {
        long minMover = mover;
        long minEnemy = enemy;

        for (int r = 1; r < 8; r++) {
            final long rMover = BitBoardUtils.reflection(mover, r);
            final long rEnemy = BitBoardUtils.reflection(enemy, r);
            if (rMover < minMover || (rMover == minMover && rEnemy < minEnemy)) {
                minMover = rMover;
                minEnemy = rEnemy;
            }
        }

        this.mover = minMover;
        this.enemy = minEnemy;
    }

    /**
     * Convert this minimal reflection to a nominal position with black to move
     *
     * @return the Board.
     */
    public Board toBoard() {
        return toBoard(true);
    }

    public Board toBoard(boolean blackToMove) {
        if (blackToMove) {
            return new Board(mover, enemy, true);
        } else {
            return new Board(enemy, mover, false);
        }
    }

    /**
     * Get subpositions of this position.
     * <p/>
     * A position may be added more than once if it is identical, by reflection,
     * to another subposition of the original position. For example, calling this method
     * on the start position will result in an array with 4 identical elements.
     *
     * @return subPositions of this position
     */
    public List<MinimalReflection> subPositions() {
        final Board pos = new Board(mover, enemy, true);
        final List<MinimalReflection> subPositions = new ArrayList<>();

        long moves = calcMoves();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            final long placement = 1L << sq;
            moves &= ~placement;

            final Board subBoard = pos.play(sq);
            subPositions.add(new MinimalReflection(subBoard.mover(), subBoard.enemy()));
        }

        return subPositions;
    }

    public long calcMoves() {
        return BitBoardUtils.calcMoves(mover, enemy);
    }


    @Override public String toString() {
        return toBoard().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MinimalReflection mr = (MinimalReflection) o;

        return enemy == mr.enemy && mover == mr.mover;

    }

    @Override
    public int hashCode() {
        return (int) NovelloUtils.hash(mover, enemy);
    }

    public int nEmpty() {
        return Long.bitCount(empty());
    }

    private long empty() {
        return ~(mover | enemy);
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeLong(mover);
        out.writeLong(enemy);
    }

    public static final ObjectFeed.Deserializer<MinimalReflection> deserializer = new ObjectFeed.Deserializer<MinimalReflection>() {
        @Override public MinimalReflection read(DataInputStream in) throws IOException {
            final long mover = in.readLong();
            final long enemy = in.readLong();

            return new MinimalReflection(mover, enemy);
        }
    };
}
