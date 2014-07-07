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

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Board;
import com.welty.novello.core.MutableGame;
import com.welty.othello.gdk.OsClock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Othello SyncEngine
 * <p/>
 * Implementations do NOT need to be thread-safe.
 */
public interface SyncEngine {
    /**
     * Determine the player's next move.
     * <p/>
     * Precondition: the game's current position has a legal move
     *
     * @param game    game to move in
     * @param maxDepth maximum search depth, in ply, in the midgame. The engine determines search depth in the endgame.  @return square and value of the player's chosen move
     */
    @NotNull MoveScore calcMove(@NotNull MutableGame game, int maxDepth);

    /**
     * Determine the player's next move.
     * <p/>
     * Precondition: the board has a legal move
     *
     * @param board    board to move on
     * @param maxDepth maximum search depth, in ply, in the midgame. The engine determines search depth in the endgame.  @return square and value of the player's chosen move
     */
    @NotNull MoveScore calcMove(@NotNull Board board, @Nullable OsClock clock, int maxDepth);

    /**
     * Clear all transposition tables and state
     * <p/>
     * This is used to ensure that experiment results are unbiased by previous games
     */
    void clear();
}
