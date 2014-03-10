package com.welty.novello.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal reflection of a position
 */
public class Mr {
    public final long mover;
    public final long enemy;

    public Mr(long mover, long enemy) {
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

    public Board toPosition() {
        return new Board(mover, enemy, true);
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
    public List<Mr> subPositions() {
        final Board pos = new Board(mover, enemy, true);
        final List<Mr> subPositions = new ArrayList<>();

        long moves = calcMoves();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            final long placement = 1L << sq;
            moves &= ~placement;

            final Board subBoard = pos.play(sq);
            subPositions.add(new Mr(subBoard.mover(), subBoard.enemy()));
        }

        return subPositions;
    }

    public long calcMoves() {
        return BitBoardUtils.calcMoves(mover, enemy);
    }


    @Override public String toString() {
        return toPosition().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mr mr = (Mr) o;

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

    public static final ObjectFeed.Deserializer<Mr> deserializer = new ObjectFeed.Deserializer<Mr>() {
        @Override public Mr read(DataInputStream in) throws IOException {
            final long mover = in.readLong();
            final long enemy = in.readLong();

            return new Mr(mover, enemy);
        }
    };
}
