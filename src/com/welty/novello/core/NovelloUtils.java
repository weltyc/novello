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

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 */
public class NovelloUtils {
    /**
     * This is the score used when no move has yet been evaluated. It needs to be lower than
     * any valid score. But it also needs to be well away from the bounds for an int, so we can add MPC margins to
     * it and not overflow.
     */
    public static final int NO_MOVE = Integer.MIN_VALUE >> 1;

    /**
     * Index of units char in prefixes
     */
    private static long murmurMix(long h) {
        h *= 0xc6a4a7935bd1e995L;
        h ^= h >>> 47 | h << 17;
        h *= 0xc6a4a7935bd1e995L;
        return h;
    }

    /**
     * Hash 2 longs into a single long, using a mixing function derived from Murmur.
     *
     * @return hash
     */
    public static long hash(long mover, long enemy) {
        final long a = murmurMix(mover ^ murmurMix(enemy));
        return a ^ (a>>>47);
    }

    public static String getHostName() {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "Unknown";
        }
        return hostName;
    }

    /**
     * Get the user name from system.getProperty
     *
     * @return the user name
     */
    public static @NotNull String getUserName() {
        return System.getProperty("user.name");
    }

    public static String getShortOptions(String[] args) {
        StringBuilder options = new StringBuilder();
        for (String arg : args) {
            if (arg.startsWith("-")) {
                options.append(arg.substring(1));
            }
        }
        return options.toString();
    }
}
