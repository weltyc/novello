package com.welty.ggf;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;

import java.text.DecimalFormat;

/**
 */
public class Move {
    /**
     * Square of the move.
     *
     * This may be longer than 2 characters; however only the first two characters are used to determine
     * the play location.
     */
    private final String square;

    /**
     * Time taken, in seconds
     */
    public final double time;

    /**
     * Evaluation returned by the engine
     */
    public final double eval;

    /**
     * Generic pass move
     * <p/>
     * This is a pass move with no eval and no time elapsed.
     * To create a pass move with an eval or time elapsed, use the constructor.
     */
    public static final Move PASS = new Move("PASS");

    public Move(String text) {
        final String[] split = text.split("/");
        if (split.length > 3) {
            throw new IllegalArgumentException("Moves may have at most 3 components");
        }
        square = split[0];
        eval = (split.length > 1 && !split[1].isEmpty()) ? Double.parseDouble(split[1]) : 0;
        time = (split.length > 2 && !split[2].isEmpty()) ? Double.parseDouble(split[2]) : 0;
    }

    public Move(MoveScore moveScore, double time) {
        square = BitBoardUtils.sqToText(moveScore.sq);
        this.eval = moveScore.score * .01;
        this.time = time;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        appendTo(sb);
        return sb.toString();
    }

    public void appendTo(StringBuilder sb) {
        sb.append(square);
        if (time != 0 || eval != 0) {
            sb.append('/');
            if (eval != 0) {
                sb.append(String.format("%.2f", eval));
            }
            if (time != 0) {
                sb.append('/');
                sb.append(new DecimalFormat("#0.00#").format(time));
            }
        }
    }

    /**
     * Square of the move, or -1 if the move was a pass
     *
     * This only returns a sensible value on an 8x8 board.
     */
    public int getSq() {
        return isPass() ? -1 : BitBoardUtils.textToSq(square.substring(0,2));
    }

    /**
     * @return true if the move is a pass move
     */
    public boolean isPass() {
        return square.toUpperCase().startsWith("PA");
    }
}