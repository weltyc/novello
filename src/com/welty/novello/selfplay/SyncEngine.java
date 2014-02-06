package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Othello SyncEngine
 * <p/>
 * Implementations do NOT need to be thread-safe.
 */
public interface SyncEngine {
    /**
     * Determine the player's next move.
     * <p/>
     * Precondition: the board has a legal move
     *
     * @param board board to move on
     * @return square and value of the player's chosen move
     */
    MoveScore calcMove(@NotNull Position board);

    /**
     * Set the maximum search depth for the player.
     * <p/>
     * The maxDepth may be greater than 64; in particular it may be set to Integer.MAX_VALUE to indicate
     * no search depth limit.
     * <p/>
     * If maxDepth is negative the result is undefined.
     *
     * @param maxDepth maximum search depth, in ply
     */
    void setMaxDepth(int maxDepth);

    /**
     * Clear all transposition tables and state
     * <p/>
     * This is used to ensure that experiment results are unbiased by previous games
     */
    void clear();
}
