package com.welty.novello.solver;

import com.welty.novello.core.Square;

/**
 * Counts the number of times square.calcFlips() was called in a thread.
 *
 * Not thread safe.
 */
class CountingFlipCalc {
    private long nFlips = 0;

    public long nFlips() {
        return nFlips;
    }

    public long calcFlips(Square square, long mover, long enemy) {
        nFlips++;
        return square.calcFlips(mover, enemy);
    }
}
