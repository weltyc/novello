package com.welty.othello.api;

import com.orbanova.common.misc.ListenerManager;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

/**
 * An Engine that communicates in a stateless manner.
 * <p/>
 * Engine requests (hints or moves) depend on a pingPong and a shared state. Callbacks from the Engine
 * that depend on shared state (board position, engine state) take a "pong" argument. The caller checks that ping==pong when receiving a
 * message; if it is, the message relates to the current state.
 * <p/>
 * Callbacks may occur on any thread, and are not guaranteed to be on the same thread every time. Thread-safety is
 * maintained by the caller checking ping==pong.
 */
public abstract class StatelessEngine extends ListenerManager<StatelessEngine.Listener> {
    public abstract void terminate();

    public abstract void learn(PingPong pingPong, SearchState state);

    public abstract void requestHints(PingPong pingPong, SearchState state, int nMoves);

    public abstract void requestMove(PingPong pingPong, SearchState state);

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

    protected void fireHint(int pong, boolean book, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
        for (StatelessEngine.Listener l : getListeners()) {
            l.hint(pong, book, pv, move, eval, nGames, depth, freeformText);
        }
    }

    /**
     * Notify listeners of a status update
     */
    protected void fireStatusChanged() {
        for (Listener l : getListeners()) {
            l.statusChanged();
        }
    }

    /**
     * Notify listeners that the engine moved
     *
     * @param mli move
     */
    protected void fireEngineMove(int pong, OsMoveListItem mli) {
        for (Listener l : getListeners()) {
            l.engineMove(pong, mli);
        }
    }

    /**
     * Notify listeners that the engine has become ready
     */
    protected void fireEngineReady(int pong) {
        for (Listener l : getListeners()) {
            l.engineReady(pong);
        }
    }


    protected void fireParseError(int pong, String command, String errorMessage) {
        for (Listener l : getListeners()) {
            l.parseError(pong, command, errorMessage);
        }
    }

    /**
     * Listens to responses from the Engine.
     * <p/>
     * The pong is a part of all responses; the Listener is responsible for knowing whether the
     * given pong is up to date. Thus the Listener is responsible for keeping track of the current
     * ping and pong.
     */
    public interface Listener {
        /**
         * The Engine updated its status
         * <p/>
         * call getStatus() to get the status. This is because the status is independent of the ping state.
         */
        public void statusChanged();

        /**
         * The engine moved.
         * <p/>
         * The engine only sends this message if it relates to the current board position (ping = pong).
         * Otherwise it discards the message.
         *
         * @param mli engine move
         */
        void engineMove(int pong, OsMoveListItem mli);

        /**
         * The engine is now ready to accept new commands (it has no backlog).
         */
        void engineReady(int pong);

        /**
         * The engine's evaluation of a move.
         * <p/>
         * The engine only sends this message if it relates to the current board position (ping = pong).
         * Otherwise it discards the message.
         *
         * @param fromBook     if true, hint comes from the book
         * @param pv           principal variation - the first two characters are the evaluated move.
         * @param move         the evaluated move
         * @param eval         evaluation of the move.
         * @param nGames       # of games (for book moves only)
         * @param depth        search depth reached when evaluating this move
         * @param freeformText optional extra text relating to the move
         */
        void hint(int pong, boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText);

        /**
         * The engine sent a message which appears to be an nboard protocol message but can't be parsed correctly.
         *
         * @param command      command from engine
         * @param errorMessage error message from parser
         */
        void parseError(int pong, String command, String errorMessage);
    }
}
