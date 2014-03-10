package com.welty.novello.core;

import com.welty.othello.gdk.OsClock;
import org.jetbrains.annotations.NotNull;

/**
 * Board position + clocks.
 * <p/>
 * Immutable.
 */
public class State {
    public final @NotNull Position position;
    public final @NotNull OsClock blackClock;
    public final @NotNull OsClock whiteClock;

    public State(@NotNull Position position, @NotNull OsClock blackClock, @NotNull OsClock whiteClock) {
        this.position = position;
        this.blackClock = blackClock;
        this.whiteClock = whiteClock;
    }

    public State playOrPass(Move8x8 move) {
        final Position nextPosition = position.playOrPass(move.getSq());
        if (position.blackToMove) {
            return new State(nextPosition, blackClock.update(move.time), whiteClock);
        } else {
            return new State(nextPosition, blackClock, whiteClock.update(move.time));
        }
    }
}
