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
