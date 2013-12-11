package com.welty.novello.core;

/**
 */
public class NovelloUtils {
    /**
     * This is the score used when no move has yet been evaluated. It needs to be lower than
     * any valid score. But it also needs to be well away from the bounds for an int, so we can add MPC margins to
     * it and not overflow.
     */
    public static final int NO_MOVE = Integer.MIN_VALUE >> 1;

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
        return murmurMix(mover ^ murmurMix(enemy));
    }
}
