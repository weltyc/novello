package com.welty.othello.api;

import org.jetbrains.annotations.NotNull;

/**
 * Handle requests to the engine in a stateless manner.
 * <p/>
 * Engine requests ('go' or 'hint') depend on a pingPong and a shared state. Callbacks from the Engine
 * that depend on shared state (board position, engine state) take a "pong" argument. The caller checks that ping==pong when receiving a
 * message; if it is, the message relates to the current state.
 * <p/>
 * This interface handles requests to the engine. Responses are handled separately.
 */
public interface StatelessEngine {
    public abstract void terminate();

    public abstract void learn(PingPong pingPong, NBoardState state);

    public abstract void requestHints(PingPong pingPong, NBoardState state, int nMoves);

    public abstract void requestMove(PingPong pingPong, NBoardState state);

    public abstract @NotNull String getName();

    public abstract @NotNull String getStatus();

    /**
     * Determine if the engine can accept new commands
     * <p/>
     * If the engine is falling behind, it should return "false" until it is caught up, then fireEngineReady()
     * so its listeners know they can call it again.
     *
     * @return true if the engine is ready to accept more commands
     */
    public abstract boolean isReady();
}
