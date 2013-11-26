package com.welty.novello.selfplay;

import com.welty.novello.solver.BitBoard;
import org.jetbrains.annotations.NotNull;

/**
 */
public interface Player {
    public static final int FLAG_PRINT_SCORE = 1;
    public static final int FLAG_PRINT_SEARCH = 2;

    /**
     * Determine the player's next move.
     * <p/>
     * Precondition: the board has a legal move
     *
     * @param board      board to move on
     * @param moverMoves legal moves for the player to move
     * @param flags      a sum of binary flags. 1 = print out move score.
     * @return square of the player's chosen move
     */
    MoveScore calcMove(@NotNull BitBoard board, long moverMoves, int flags);
}
