package com.welty.othello.gui;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.selfplay.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 */
class Engine {
    private final @NotNull Player player;
    private final @NotNull RequestQueue queue = new RequestQueue();

    Engine(@NotNull Player player) {
        this.player = player;
        new Thread(queue).start();
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
        final MoveRequest moveRequest = new MoveRequest(gameView, position, ping);
        queue.add(moveRequest);
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

    private static class MoveRequest {
        private final @NotNull GameView gameView;
        private final @NotNull Position position;
        private final long ping;

        private MoveRequest(@NotNull GameView gameView, @NotNull Position position, long ping) {
            this.gameView = gameView;
            this.position = position;
            this.ping = ping;
        }

        private void respond(Player player) {
            final long moves = position.calcMoves();
            final MoveScore moveScore;
            if (moves == 0) {
                moveScore = new MoveScore(-1, 0);
            } else {
                moveScore = player.calcMove(position, moves, 0);
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    gameView.engineMove(moveScore, ping);
                }
            });
        }
    }

    /**
     * Only holds one request. There's no reason for the engine to spend time thinking
     * about positions that are no longer on the board, so outdated positions are just discarded.
     */
    private class RequestQueue implements Runnable {
        private @Nullable MoveRequest request = null;

        private synchronized void add(MoveRequest request) {
            this.request = request;
            notifyAll();
        }

        private synchronized @NotNull MoveRequest take() {
            while (true) {
                if (request != null) {
                    final MoveRequest take = request;
                    request = null;
                    return take;
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignore.
                }
            }
        }


        @Override public void run() {
            while (true) {
                final MoveRequest request = take();
                request.respond(player);
            }
        }
    }

}
