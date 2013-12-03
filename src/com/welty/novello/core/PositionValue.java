package com.welty.novello.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Position tagged with a value, for use in coefficient calcs
 */
public class PositionValue {
    public final long mover;
    public final long enemy;
    public final int value;

    /**
     * Construct from bitboards and score to mover
     *
     * Precondition: mover has a legal move
     *
     * @param mover mover bitboard
     * @param enemy enemy bitboard
     * @param value net score; + means mover is ahead.
     */
    public PositionValue(long mover, long enemy, int value) {
        final long moves = BitBoardUtils.calcMoves(mover, enemy);
        if (moves==0) {
            System.err.println(new Position(mover, enemy, false));
            throw new IllegalArgumentException("must have a legal move");
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

    public static final ObjectFeed.Deserializer<PositionValue> deserializer = new ObjectFeed.Deserializer<PositionValue>() {
        @Override public PositionValue read(DataInputStream in) throws IOException {
            final long mover = in.readLong();
            final long enemy = in.readLong();
            final int value = in.readInt();

            return new PositionValue(mover, enemy, value);
        }
    };
}
