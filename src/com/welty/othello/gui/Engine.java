package com.welty.othello.gui;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.selfplay.Player;
import org.jetbrains.annotations.NotNull;

/**
 */
class Engine {
    private final @NotNull Player player;

    Engine(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Notify the engine that it should move.
     * <p/>
     * The Engine moves by calling GameView.engineMove() with its move and the given ping.
     * <p/>
     * This method may be called from the Event Dispatch Thread. To keep the GUI responsive, the engine should
     * not perform lengthy calculations in the calling thread. The Engine will normally perform computations in another
     * thread and call GameView.engineMove() once the computation is complete.
     * <p/>
     * The user may have changed the gameView between the time this method is called and when the Engine
     * calls GameView.engineMove(). To ensure the Engine is calling engineMove() with a move for the correct position,
     * the GameView ignores engineMove() calls that have outdated pings.
     *
     * @param gameView move requester.
     * @param position position for which a move is requested.
     * @param ping     parameter that must be passed to GameView.engineMove() to validate the move
     */
    public void requestMove(@NotNull GameView gameView, @NotNull Position position, long ping) {
        // todo execute in a new thread
        final long moves = position.calcMoves();
        if (moves == 0) {
            gameView.engineMove(new MoveScore(-1, 0), ping);
        } else {
            final MoveScore moveScore = player.calcMove(position, moves, 0);
            gameView.engineMove(moveScore, ping);
        }
    }

    /**
     * Get the Engine's name
     * <p/>
     * This is the engine name as it will be displayed in the GUI and the GGF PlayerName field, for instance. Typically
     * depth information is included, for instance "ntest:8".
     *
     * @return the Engine's name
     */
    public @NotNull String getName() {
        return player.toString();
    }
}
