package com.welty.novello.core;

import com.welty.othello.gdk.OsMove;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.protocol.Depth;
import com.welty.othello.protocol.HintResponse;
import com.welty.othello.protocol.Value;
import org.jetbrains.annotations.NotNull;

/**
 * A move with an evaluation
 */
public class MoveScore {
    public final int sq;
    public final int centidisks;

    /**
     * @param sq         square index of the move
     * @param centidisks evaluation, from mover's point of view, in centidisks
     */
    public MoveScore(int sq, int centidisks) {
        this.sq = sq;
        this.centidisks = centidisks;
    }

    /**
     * @param squareText square of move, in text format; for example "D5"
     * @param centidisks evaluation, from mover's point of view, in centidisks
     */
    public MoveScore(String squareText, int centidisks) {
        this(BitBoardUtils.textToSq(squareText), centidisks);
    }

    @Override public String toString() {
        return BitBoardUtils.sqToText(sq) + "/" + centidisks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoveScore moveScore = (MoveScore) o;

        return centidisks == moveScore.centidisks && sq == moveScore.sq;

    }

    @Override
    public int hashCode() {
        int result = sq;
        result = 31 * result + centidisks;
        return result;
    }

    /**
     * Convert this MoveScore to an OsMoveListItem.
     *
     * @param millis elapsed time, in milliseconds
     * @return the OsMoveListItem
     */
    public OsMoveListItem toMli(long millis) {
        final OsMove move = getOsMove();
        final double eval = 0.01 * centidisks;
        return new OsMoveListItem(move, eval, 0.001*millis);
    }

    public @NotNull OsMove getOsMove() {
        return new OsMove(BitBoardUtils.sqToText(sq));
    }

    public HintResponse toHintResponse(int pong, Depth depth) {
        final String pv = BitBoardUtils.sqToText(sq);
        final Value eval = new Value(0.01f * centidisks);
        return new HintResponse(pong, false, pv, eval, 0, depth, "");
    }
}
