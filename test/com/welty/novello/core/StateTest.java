package com.welty.novello.core;

import com.welty.othello.gdk.OsClock;
import junit.framework.TestCase;

/**
 */
public class StateTest extends TestCase {
    public void testUpdate() {
        final State state = new State(Position.START_POSITION, new OsClock(0.5), new OsClock(0.6));
        checkClocks(state, 0.5, 0.6);
        final State state1 = state.playOrPass(new Move8x8("F5//0.01"));
        checkClocks(state1, 0.49, 0.6);
        final State state2 = state1.playOrPass(new Move8x8("D6//1"));
        checkClocks(state2, 0.49, 119.6);
        assertEquals(1, state2.whiteClock.getITimeout());
    }

    private void checkClocks(State state, double blackRemaining, double whiteRemaining) {
        assertEquals(blackRemaining, state.blackClock.tCurrent);
        assertEquals(whiteRemaining, state.whiteClock.tCurrent);
    }
}
