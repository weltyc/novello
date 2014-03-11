package com.welty.novello.selfplay;

import com.welty.novello.core.Board;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.MutableGame;
import com.welty.othello.gdk.OsClock;
import org.jetbrains.annotations.Nullable;

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
     * @param board board to move on
     * @param clock amount of time remaining, or null if the player should ignore the clock
     * @return square and value of the player's chosen move
     */
    public MoveScore calcMove(Board board, @Nullable OsClock clock) {
        return engine.calcMove(board, clock, maxDepth);
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
