package com.welty.novello.core;

import com.welty.ggf.Move;
import com.welty.novello.eval.CoefficientCalculator;

/**
 */
public class Move8x8 extends Move {
    /**
     * Generic pass move
     * <p/>
     * This is a pass move with no eval and no time elapsed.
     * To create a pass move with an eval or time elapsed, use the constructor.
     */
    public static final Move8x8 PASS = new Move8x8("PASS");

    /**
     * Create an 8x8 move
     *
     * @param text text of the move, e.g. "d5/1/1.2" or "pass"
     * @throws IllegalArgumentException if the move is not an 8x8 move.
     */
    public Move8x8(String text) {
        super(text);
        getSq();
    }

    /**
     * Create an 8x8 move
     *
     * @param moveScore move text and score
     * @param time      elapsed time, in seconds
     * @throws IllegalArgumentException if the move is not an 8x8 move.
     */
    public Move8x8(MoveScore moveScore, double time) {
        super(BitBoardUtils.sqToText(moveScore.sq), moveScore.centidisks / (double) CoefficientCalculator.DISK_VALUE, time);
        getSq();
    }

    /**
     * Square of the move, or -1 if the move was a pass
     * <p/>
     * This only returns a sensible value on an 8x8 board.
     */
    public int getSq() {
        return isPass() ? -1 : BitBoardUtils.textToSq(getSquare());
    }
}
