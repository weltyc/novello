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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Random test positions, 'early' with about 16 disks and 'late' with about 48 disks
 */
public class Me {
    public final long mover;
    public final long enemy;

    /**
     * A test position with 15 disks & 49 empties
     */
    public static final Me early;
    /**
     * A test position with about 48 disks & 16 empties
     */
    public static final Me late;

    /**
     * A test position with about 32 disks & 32 empties
     */
    public static final Me mid;

    static {
        final Random r = new Random(1337);
        late = late(r);
        early = early(r);
        mid = mid(r);
    }

    /**
     * Generate a test position with about 16 disks & 48 empties
     *
     * @param random random number generator
     * @return the test position
     */
    public static Me early(Random random) {
        return new Me(random, random.nextLong() | random.nextLong());
    }

    /**
     * Generate a test position with about 32 disks & 32 empties
     *
     * @param random random number generator
     * @return the test position
     */
    public static Me mid(Random random) {
        return new Me(random, random.nextLong());
    }

    /**
     * Generate a test position with about 48 disks & 16 empties
     *
     * @param random random number generator
     * @return the test position
     */
    public static Me late(Random random) {
        return new Me(random, random.nextLong() & random.nextLong());
    }

    /**
     * Generate a Me with the given empty squares
     *
     * @param empty empty squares
     */
    public Me(Random r, long empty) {
        mover = ~empty & r.nextLong();
        enemy = ~(mover | empty);
    }

    public Me(long mover, long enemy) {
        this.mover = mover;
        this.enemy = enemy;
    }

    public Board toPosition() {
        return new Board(mover, enemy, true);
    }

    /**
     * @return subPositions of this position
     */
    public Collection<Me> subPositions() {
        final Board pos = new Board(mover, enemy, true);
        final ArrayList<Me> subPositions = new ArrayList<>();

        long moves = calcMoves();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            final long placement = 1L << sq;
            moves &= ~placement;

            final Board subBoard = pos.play(sq);
            subPositions.add(new Me(subBoard.mover(), subBoard.enemy()));
        }

        return subPositions;
    }

    public long calcMoves() {
        return BitBoardUtils.calcMoves(mover, enemy);
    }


    public long enemyMoves() {
        return BitBoardUtils.calcMoves(enemy, mover);
    }


    /**
     * Choose one of the 8 reflections of this position
     *
     * @return the minimal reflection
     */
    public Me minimalReflection() {
        Me minimal = this;
        for (int r = 1; r < 8; r++) {
            final long rMover = BitBoardUtils.reflection(mover, r);
            final long rEnemy = BitBoardUtils.reflection(enemy, r);
            if (rMover < minimal.mover || (rMover == minimal.mover && rEnemy < minimal.enemy)) {
                minimal = new Me(rMover, rEnemy);
            }
        }
        return minimal;
    }

    @Override public String toString() {
        return toPosition().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Me me = (Me) o;

        return enemy == me.enemy && mover == me.mover;

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

    public static void main(String[] args) {
        System.out.println(early.nEmpty());
        System.out.println(late.nEmpty());
    }
}
