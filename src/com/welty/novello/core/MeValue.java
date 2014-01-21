package com.welty.novello.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Position tagged with a value, for use in coefficient calcs
 */
public class MeValue {
    public final long mover;
    public final long enemy;

    /**
     * Position value to mover, in centidisks
     */
    public final int value;

    /**
     * Construct from bitboards and score to mover
     *
     * Precondition: mover has a legal move
     *
     * @param mover mover bitboard
     * @param enemy enemy bitboard
     * @param value net score, in centidisks; + means mover is ahead.
     */
    public MeValue(long mover, long enemy, int value) {
        final long moves = BitBoardUtils.calcMoves(mover, enemy);
        if (moves==0) {
            throw new IllegalArgumentException("must have a legal move");
        }
        if ((mover&enemy)!=0) {
            throw new IllegalArgumentException("mover and enemy overlap");
        }
        this.mover = mover;
        this.enemy = enemy;
        this.value = value;
    }

    public int nEmpty() {
        return BitBoardUtils.nEmpty(mover, enemy);
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeLong(mover);
        out.writeLong(enemy);
        out.writeInt(value);
    }

    public static final ObjectFeed.Deserializer<MeValue> deserializer = new ObjectFeed.Deserializer<MeValue>() {
        @Override public MeValue read(DataInputStream in) throws IOException {
            final long mover = in.readLong();
            final long enemy = in.readLong();
            final int value = in.readInt();

            return new MeValue(mover, enemy, value);
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeValue that = (MeValue) o;

        return enemy == that.enemy && mover == that.mover && value == that.value;

    }

    @Override
    public int hashCode() {
        return (int)NovelloUtils.hash(mover, enemy);
    }

    @Override public String toString() {
        return new Position(mover, enemy, true) + "\n" + value;
    }

    /**
     * @return Minimal reflection of the position
     */
    public Mr toMr() {
        return new Mr(mover, enemy);
    }
}
