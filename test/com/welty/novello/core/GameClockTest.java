package com.welty.novello.core;

import com.orbanova.common.date.Time;
import junit.framework.TestCase;

/**
 */
public class GameClockTest extends TestCase {
    public void testStringConstructor() {
        testStringConstructor("01:00/00:00/02:00", 1 * Time.MINUTE);  // from https://skatgame.net/mburo/ggsa/ggf
        testStringConstructor("05:00//02:00", 5 * Time.MINUTE);  // from ggf game download
        testStringConstructor("0:02", 2 * Time.SECOND);  // last two params optional
        testStringConstructor("2", 2 * Time.SECOND);  // minutes optional
        testStringConstructor("4:00:00", 4 * Time.HOUR);  // hours
    }

    private static void testStringConstructor(String time, long expected) {
        final GameClock clock = new GameClock(time);
        assertEquals(expected, clock.remaining);
    }

    public void testToString() {
        testToString("01:00/00:00/02:00", "01:00//02:00");  // from https://skatgame.net/mburo/ggsa/ggf
        testToString("05:00//02:00", "05:00//02:00");  // from ggf game download
        testToString("0:02", "00:02");  // last two params optional
        testToString("2", "00:02");  // minutes optional
        testToString("4:00:00", "04:00:00");  // hours
    }

    public void testToString(String in, String out) {
        final GameClock clock = new GameClock(in);
        assertEquals(out, clock.toString());
    }

    public void testUpdate() {
        final GameClock gameClock = new GameClock("05:00").update(30);
        assertEquals("04:30", gameClock.toString());

    }
}
