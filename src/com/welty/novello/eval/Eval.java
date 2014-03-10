package com.welty.novello.eval;

import com.welty.novello.core.Board;

/**
 * Evaluates a position.
 *
 * This class is thread-safe. If your implementation is not thread-safe, it should be a Counter.
 */
public abstract class Eval {
    public abstract int eval(long mover, long enemy);

    /**
     * Evaluate a position
     * <p/>
     * This function will check for passes and return the terminal value if the game is over.
     *
     * @param board@return value of position.
     */
    public int eval(Board board) {
        return eval(board.mover(), board.enemy());
    }
}
