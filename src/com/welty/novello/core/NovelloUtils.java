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
    private static char[] prefixes = " kMGTPE".toCharArray();
    private static char[] negPrefixes = " m\u03BCnpa".toCharArray();

    /**
     * Index of units char in prefixes
     */
    private static int prefixOffset = new String(prefixes).indexOf(' ');

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

    // Engineering format - double

    public static String engineeringDouble(double x) {
        return engineeringDouble(x, calcPrefix(x));
    }

    static int calcPrefix(double x) {
        x = Math.abs(x);
        double prefix = 1;
        int prefixIndex = 0;
        final double limit = 999.995;
        if (x!=0) {
            while (x/prefix < limit/1000) {
                prefix /= 1000;
                prefixIndex--;
            }
        }
        while (x /prefix>= limit) {
            prefix*=1000;
            prefixIndex++;
        }
        return prefixIndex;
    }

    public static String engineeringDouble(double x, int prefixIndex) {
        x *= Math.pow(1000, -prefixIndex);
        return String.format("%7.2f %c", x, prefixIndex >0 ? prefixes[prefixIndex] : negPrefixes[-prefixIndex]);
    }

    // Engineering format - long

    public static String engineeringLong(long x) {
        return engineeringLong(x, calcPrefix(x));
    }

    public static String engineeringLong(long nFlips, int prefixIndex) {
        for (int i=0; i<prefixIndex; i++) {
            nFlips /=1000;
        }
        return String.format("%,6d %c", nFlips, prefixes[prefixIndex]);
    }

    static int calcPrefix(long x) {
        x = Math.abs(x);
        int prefix = 1;
        int prefixIndex = 0;
        final long limit = 100000;
        while (x /prefix>= limit) {
            prefix*=1000;
            prefixIndex++;
        }
        return prefixIndex;
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
