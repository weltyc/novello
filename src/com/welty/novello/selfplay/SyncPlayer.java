package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;

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
     * @return square and value of the player's chosen move
     */
    public MoveScore calcMove(Position board) {
        return engine.calcMove(board, maxDepth);
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
}
