package com.welty.novello.core;

import com.welty.othello.gdk.OsClock;
import org.jetbrains.annotations.NotNull;

/**
 * Board position + clocks.
 * <p/>
 * Immutable.
 */
public class State {
    public final @NotNull Board board;
    public final @NotNull OsClock blackClock;
    public final @NotNull OsClock whiteClock;

    public State(@NotNull Board board, @NotNull OsClock blackClock, @NotNull OsClock whiteClock) {
        this.board = board;
        this.blackClock = blackClock;
        this.whiteClock = whiteClock;
    }

    public State playOrPass(Move8x8 move) {
        final Board nextBoard = board.playOrPass(move.getSq());
        if (board.blackToMove) {
            return new State(nextBoard, blackClock.update(move.getElapsedTime()), whiteClock);
        } else {
            return new State(nextBoard, blackClock, whiteClock.update(move.getElapsedTime()));
        }
    }
}
