package com.welty.othello.api;

import com.welty.othello.engine.ExternalNBoardEngine;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.protocol.ResponseHandler;
import com.welty.othello.protocol.ResponseParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Class that controls communication with an engine.
 */
public class ParsedEngine implements StatelessEngine {
    /**
     * The last ping sent to this engine.
     * <p/>
     * This is used to determine whether the engine is caught up. When there are multiple engines,
     * this variable only cares about its engine.
     */
    private volatile int lastPing;
    private final @NotNull NBoardEngine engine;
    private final @NotNull ResponseParser responseParser;

    public ParsedEngine(String[] command, File workingDirectory, boolean debug, @NotNull ResponseHandler responseHandler) throws IOException {
        responseParser = new ResponseParser(responseHandler, command[0]);
        engine = new ExternalNBoardEngine(command, workingDirectory, debug, responseParser);

    }

    @NotNull public String getName() {
        return responseParser.getName();
    }

    /**
     * Request hints (evaluation of the top n moves) from the engine, for the current board
     *
     * @param nMoves number of moves to evaluate
     */
    @Override public synchronized void requestHints(PingPong pingPong, SearchState state, int nMoves) {
        updateEngineState(pingPong, state);
        engine.sendCommand("hint " + nMoves);
    }

    /**
     * Tell the Engine to learn the current game.
     */
    @Override public synchronized void learn(PingPong pingPong, SearchState state) {
        updateEngineState(pingPong, state);
        engine.sendCommand("learn");
    }

    /**
     * Request a valid move from the engine, for the current board.
     * <p/>
     * Unlike {@link #requestHints(PingPong, SearchState, int)}, the engine does not have to return an evaluation;
     * if it has only one legal move it may choose to return that move immediately without searching.
     */
    @Override public synchronized void requestMove(PingPong pingPong, SearchState state) {
        updateEngineState(pingPong, state);
        engine.sendCommand("go");
    }

    /**
     * Terminate the thread that sends messages to the window.
     * <p/>
     * This is called when the OS copy of the window is about to be destroyed. Sending
     * additional messages to the window could result in crashes.
     */
    @Override public void terminate() {
        engine.sendCommand("quit");
    }

    @Override public @NotNull String getStatus() {
        return responseParser.getStatus();
    }

    @Override public boolean isReady() {
        return responseParser.getPong() >= lastPing;
    }

    /**
     * Set the NBoard protocol's current game.
     *
     * @param game game to set.
     */
    private void setGame(COsGame game) {
        engine.sendCommand("set game " + game);
    }

    /**
     * Set the engine's contempt factor (scoring of proven draws).
     *
     * @param contempt contempt, in centidisks.
     */
    private void setContempt(int contempt) {
        engine.sendCommand("set contempt " + contempt);
    }


    private void setMaxDepth(int maxDepth) {
        engine.sendCommand("set depth " + maxDepth);
    }

    /**
     * Append a move to the NBoard protocol's current game.
     *
     * @param mli the move to append to the protocol's current game.
     */
    private void sendMove(OsMoveListItem mli) {
        engine.sendCommand("move " + mli);
    }

    private void updateEngineState(PingPong pingPong, SearchState state) {
        setGame(state.getGame());
        setContempt(state.getContempt());
        setMaxDepth(state.getMaxDepth());
        synchronized (this) {
            lastPing = pingPong.next();
            engine.sendCommand("ping " + lastPing);
        }
    }
}
