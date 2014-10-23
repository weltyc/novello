/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.core;

import com.orbanova.common.misc.Require;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.protocol.Depth;
import com.welty.othello.protocol.HintResponse;
import com.welty.othello.protocol.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A move with an evaluation
 */
public class MoveScore {
    public final int sq;
    public final int centidisks;
    /**
     * Optional Principal variation string.
     * <p/>
     * If the string is not null, it should contain no spaces.
     */
    public final @Nullable String pv;

    /**
     * @param sq         square index of the move
     * @param centidisks evaluation, from mover's point of view, in centidisks
     */
    public MoveScore(int sq, int centidisks) {
        this(sq, centidisks, null);
    }

    public MoveScore(int sq, int centidisks, @Nullable String pv) {
        this.sq = sq;
        this.centidisks = centidisks;
        this.pv = pv;
        if (pv!=null) {
            Require.isFalse(pv.contains(" "), "PV can't contain spaces but was " + pv);
        }
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
        return new OsMoveListItem(move, eval, 0.001 * millis);
    }

    public @NotNull OsMove getOsMove() {
        return new OsMove(BitBoardUtils.sqToText(sq));
    }

    public HintResponse toHintResponse(int pong, Depth depth) {
        final Value eval = new Value(0.01f * centidisks);
        return new HintResponse(pong, false, pv == null ? BitBoardUtils.sqToText(sq) : pv, eval, 0, depth, "");
    }
}
