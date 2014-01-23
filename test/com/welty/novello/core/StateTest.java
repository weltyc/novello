package com.welty.novello.core;

import junit.framework.TestCase;

/**
 */
public class StateTest extends TestCase {
    public void testUpdate() {
        final State state = new State(Position.START_POSITION, new GameClock(500, 0), new GameClock(600, 0));
        checkClocks(state, 500, 600);
        final State state1 = state.playOrPass(new Move8x8("F5//0.01"));
        checkClocks(state1, 490, 600);
        final State state2 = state1.playOrPass(new Move8x8("D6//1"));
        checkClocks(state2, 490, -400);
    }

    private void checkClocks(State state, int blackRemaining, int whiteRemaining) {
        assertEquals(blackRemaining, state.blackClock.remaining);
        assertEquals(whiteRemaining, state.whiteClock.remaining);
    }
}
