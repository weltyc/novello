package com.welty.novello.core;

/**
*/
public class Move {
    /**
     * Square of the move, or -1 if the move was a pass
     */
    public final int sq;

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
    static final Move PASS = new Move("PASS");

    Move(String text) {
        final String[] split = text.split("/");
        if (split.length > 3) {
            throw new IllegalArgumentException("Moves may have at most 3 components");
        }
        sq = split[0].toUpperCase().startsWith("PA") ? -1 : BitBoardUtils.textToSq(split[0]);
        eval = (split.length > 1 && !split[1].isEmpty()) ? Double.parseDouble(split[1]) : 0;
        time = (split.length > 2 && !split[2].isEmpty()) ? Double.parseDouble(split[2]) : 0;
    }

    public Move(MoveScore moveScore, double time) {
        this.sq = moveScore.sq;
        this.eval = moveScore.score * .01;
        this.time = time;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        appendTo(sb);
        return sb.toString();
    }

    void appendTo(StringBuilder sb) {
        sb.append(isPass() ? "PASS" : BitBoardUtils.sqToText(sq));
        if (time != 0 || eval != 0) {
            sb.append('/');
            if (eval != 0) {
                sb.append(String.format("%.2f", eval));
            }
            if (time != 0) {
                sb.append('/');
                sb.append(time);
            }
        }
    }

    public boolean isPass() {
        return sq < 0;
    }
}
