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

package com.welty.novello.selfplay;

import com.welty.novello.core.Board;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.MutableGame;

/**
 * A SyncEngine, but knows its search depth
 */
public class SyncPlayer {
    private final SyncEngine engine;
    private final int maxDepth;

    public SyncPlayer(SyncEngine engine, int maxDepth) {
        this.engine = engine;
        this.maxDepth = maxDepth;
    }

    /**
     * Determine the player's next move.
     * <p/>
     * Precondition: the board has a legal move
     *
     *
     * @param board board to move on
     * @return square and value of the player's chosen move
     */
    public MoveScore calcMove(Board board) {
        return engine.calcMove(board, null, maxDepth);
    }

    /**
     * Clear all transposition tables and state
     * <p/>
     * This is used to ensure that experiment results are unbiased by previous games
     */
    public void clear() {
        engine.clear();
    }

    @Override public String toString() {
        return engine + ":" + maxDepth;
    }

    /**
     * Determine the player's next move.
     * <p/>
     * Precondition: the game's current position has a legal move
     *
     * @param game game to move in
     * @return square and value of the player's chosen move
     */
    public MoveScore calcMove(MutableGame game) {
        return engine.calcMove(game, maxDepth);
    }
}
