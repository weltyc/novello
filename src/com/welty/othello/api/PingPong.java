package com.welty.othello.api;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracking for whether engines are up to date.
 * <p/>
 * Thread-safe.
 */
public class PingPong {
    private final AtomicInteger ping = new AtomicInteger();

    /**
     * Increment the ping value and return it
     *
     * @return ping value
     */
    public int next() {
        return ping.incrementAndGet();
    }

    /**
     * Get the current ping value
     *
     * @return ping value
     */
    public int get() {
        return ping.get();
    }
}
