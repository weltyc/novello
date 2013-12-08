package com.welty.novello.eval;

import com.welty.novello.core.Position;

/**
 */
public abstract class Eval {
    public abstract int eval(long mover, long enemy);

    /**
     * Evaluate a position
     * <p/>
     * This function will check for passes and return the terminal value if the game is over.
     *
     * @param position@return value of position.
     */
    public int eval(Position position) {
        return eval(position.mover(), position.enemy());
    }
}
