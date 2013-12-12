package com.welty.novello.core;

import java.util.*;

/**
 * Random test positions, 'early' with about 16 disks and 'late' with about 48 disks
 */
public class Me {
    public final long mover;
    public final long enemy;

    /**
     * A test position with about 16 disks & 48 empties
     */
    public static final Me early;
    /**
     * A test position with about 48 disks & 16 empties
     */
    public static final Me late;


    static {
        final Random r = new Random(1337);
        late = late(r);
        early = early(r);
    }

    /**
     * Generate a test position with about 16 disks & 48 empties
     *
     * @return the test position
     * @param random random number generator
     */
    public static Me early(Random random) {
        return new Me(random, random.nextLong() | random.nextLong());
    }

    /**
     * Generate a test position with about 48 disks & 16 empties
     *
     * @return the test position
     * @param random random number generator
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

    public Position toPosition() {
        return new Position(mover, enemy, true);
    }

    /**
     * @return subPositions of this position
     */
    public Collection<Me> subPositions() {
        final Position pos = new Position(mover, enemy, true);
        final ArrayList<Me> subPositions = new ArrayList<>();

        long moves = calcMoves();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            final long placement = 1L<<sq;
            moves &=~placement;

            final Position subPosition = pos.play(sq);
            subPositions.add(new Me(subPosition.mover(), subPosition.enemy()));
        }

        return subPositions;
    }

    public long calcMoves() {
        return BitBoardUtils.calcMoves(mover, enemy);
    }

    /**
     * Choose one of the 8 reflections of this position
     *
     * @return the minimal reflection
     */
    public Me minimalReflection() {
        Me minimal = this;
        for (int r=1; r<8; r++) {
            final long rMover = BitBoardUtils.reflection(mover, r);
            final long rEnemy = BitBoardUtils.reflection(enemy, r);
            if (rMover < minimal.mover || (rMover==minimal.mover && rEnemy < minimal.enemy)) {
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
        return (int)NovelloUtils.hash(mover, enemy);
    }

    public int nEmpty() {
        return Long.bitCount(empty());
    }

    private long empty() {
        return ~(mover|enemy);
    }
}
