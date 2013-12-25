package com.welty.novello.core;

import com.orbanova.common.date.Time;

/**
 * Represents time remaining on the clock
 */
public class GameClock {
    /**
     * Remaining time, in milliseconds
     *
     * Can be negative if a player has run out of time (or is in overtime).
     */
    public final long remaining;

    /**
     * Additional time to avoid a 64-0 forfeit
     */
    public final long overtime;

    public GameClock(long remaining, long overtime) {
        this.remaining = remaining;
        this.overtime = overtime;
    }

    public GameClock(String time) {
        final String[] parts =time.split("/");
        if (parts.length < 1 || parts.length >3) {
            throw new IllegalArgumentException("Invalid clock format: " + time);
        }
        remaining = parseTime(parts[0]);
        if (parts.length >= 2) {
            overtime = parseTime(parts[2]);
        }
        else {
            overtime = 0;
        }
    }

    private static long parseTime(String part) {
        final String[] timeParts = part.split(":");
        long seconds = 0;
        for (String timePart : timeParts) {
            seconds = seconds * 60 + Integer.parseInt(timePart);
        }
        return seconds * 1000;
    }

    @Override public String toString() {
        if (overtime != 0) {
            return format(remaining) + "//" + format(overtime);
        } else {
            return format(remaining);
        }
    }

    private String format(long remaining) {
        if (remaining < 0) {
            remaining = -remaining;
        }
        final long seconds = remaining/ Time.SECOND;
        final long s = seconds % 60;
        final long minutes = seconds / 60;
        final long m = minutes % 60;
        final long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, m, s);
        } else {
            return String.format("%02d:%02d", minutes, s);
        }
    }

    public GameClock update(double seconds) {
        final long millis = (long)Math.floor(seconds*1000+0.5);
        return new GameClock(remaining - millis, overtime);
    }
}
