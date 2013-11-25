package com.welty.novello.solver;

/**
 */
class Murmur {
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
