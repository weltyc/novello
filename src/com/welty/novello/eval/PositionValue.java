package com.welty.novello.eval;

import com.welty.novello.solver.BitBoard;
import com.welty.novello.solver.BitBoardUtils;

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
            System.err.println(new BitBoard(mover, enemy, false));
            throw new IllegalArgumentException("must have a legal move");
        }
        this.mover = mover;
        this.enemy = enemy;
        this.value = value;
    }

    public int nEmpty() {
        return BitBoardUtils.nEmpty(mover, enemy);
    }
}
