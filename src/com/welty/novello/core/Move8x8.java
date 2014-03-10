package com.welty.novello.core;

import com.welty.ggf.Move;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

/**
 */
public class Move8x8 extends OsMoveListItem {
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
    public Move8x8(@NotNull String text) {
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
    public Move8x8(@NotNull MoveScore moveScore, double time) {
        super(moveScore.getOsMove(), moveScore.centidisks / (double) CoefficientCalculator.DISK_VALUE, time);
        getSq();
    }

    /**
     * Create an 8x8 move
     *
     * @param mli move details.
     * @throws IllegalArgumentException if the move is not an 8x8 move.
     */
    public Move8x8(@NotNull OsMoveListItem mli) {
        super(mli.move, mli.getEval(), mli.getElapsedTime());
        getSq(); // throw exception if
    }

    /**
     * Square of the move, or -1 if the move was a pass
     * <p/>
     * @throws IllegalArgumentException if this move does not represent an 8x8 board.
     */
    public int getSq() {
        return move.isPass() ? -1 : BitBoardUtils.textToSq(move.toString());
    }
}
