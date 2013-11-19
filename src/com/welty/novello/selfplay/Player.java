package com.welty.novello.selfplay;

import com.welty.novello.solver.BitBoard;
import org.jetbrains.annotations.NotNull;

/**
 */
public interface Player {
    /**
     * Determine the player's next move.
     * <p/>
     * Precondition: the board has a legal move
     *
     * @param board board to move on
     * @return square of the player's chosen move
     */
    int calcMove(@NotNull BitBoard board);
}
