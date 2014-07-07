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
