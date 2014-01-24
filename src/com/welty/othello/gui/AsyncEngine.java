package com.welty.othello.gui;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.selfplay.SyncEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 */
class AsyncEngine {
    private final @NotNull SyncEngine syncEngine;
    private final @NotNull RequestQueue queue = new RequestQueue();

    AsyncEngine(@NotNull SyncEngine syncEngine) {
        this.syncEngine = syncEngine;
        new Thread(queue).start();
    }

    /**
     * Notify the engine that it should move.
     * <p/>
     * The Engine moves by calling GameModel.engineMove() with its move and the given ping.
     * <p/>
     * This method may be called from the Event Dispatch Thread. To keep the GUI responsive, the engine should
     * not perform lengthy calculations in the calling thread. The Engine will normally perform computations in another
     * thread and call GameModel.engineMove() once the computation is complete.
     * <p/>
     * The user may have changed the gameModel between the time this method is called and when the Engine
     * calls GameModel.engineMove(). To ensure the Engine is calling engineMove() with a move for the correct position,
     * the GameModel ignores engineMove() calls that have outdated pings.
     *
     * @param gameModel move requester.
     * @param position position for which a move is requested.
     * @param ping     parameter that must be passed to GameModel.engineMove() to validate the move
     */
    public void requestMove(@NotNull GameModel gameModel, @NotNull Position position, long ping) {
        final MoveRequest moveRequest = new MoveRequest(gameModel, position, ping);
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
        return syncEngine.toString();
    }

    /**
     * Set the maximum search depth for the Engine.
     *
     * @param maxDepth maximum search depth, in ply.
     */
    public void setMaxDepth(int maxDepth) {
        syncEngine.setMaxDepth(maxDepth);
    }

    private static class MoveRequest {
        private final @NotNull GameModel gameModel;
        private final @NotNull Position position;
        private final long ping;

        private MoveRequest(@NotNull GameModel gameModel, @NotNull Position position, long ping) {
            this.gameModel = gameModel;
            this.position = position;
            this.ping = ping;
        }

        private void respond(SyncEngine syncEngine) {
            final long moves = position.calcMoves();
            final MoveScore moveScore;
            if (moves == 0) {
                moveScore = new MoveScore(-1, 0);
            } else {
                moveScore = syncEngine.calcMove(position);
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    gameModel.engineMove(moveScore, ping);
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
                request.respond(syncEngine);
            }
        }
    }

}
