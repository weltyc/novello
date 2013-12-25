package com.welty.novello.core;

import org.jetbrains.annotations.NotNull;

/**
 * Board position + clocks.
 *
 * Immutable.
 */
public class State {
    public final @NotNull Position position;
    public final @NotNull GameClock blackClock;
    public final @NotNull GameClock whiteClock;

    public State(@NotNull Position position, @NotNull GameClock blackClock, @NotNull GameClock whiteClock) {
        this.position = position;
        this.blackClock = blackClock;
        this.whiteClock = whiteClock;
    }

    public State playOrPass(Move move) {
        final Position nextPosition = position.playOrPass(move.sq);
        if (position.blackToMove) {
            return new State(nextPosition, blackClock.update(move.time), whiteClock);
        }
        else {
            return new State(nextPosition, blackClock, whiteClock.update(move.time));
        }
    }
}
