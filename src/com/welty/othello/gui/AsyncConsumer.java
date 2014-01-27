package com.welty.othello.gui;

import com.welty.novello.core.MoveScore;

/**
 * Callback interface for AsyncEngine
 */
public interface AsyncConsumer {
    /**
     * Notify the consumer that the engine wishes to make a move.
     *
     * This method may be called from any thread. It is the consumer's responsibility to operate correctly
     * regardless of what thread it is called from.
     *
     * @param moveScore move Engine wishes to make and score of that move
     * @param ping id of position this move relates to.
     */
    void engineMove(MoveScore moveScore, long ping);
}
