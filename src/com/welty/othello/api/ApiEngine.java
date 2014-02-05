package com.welty.othello.api;

import com.orbanova.common.misc.ListenerManager;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveListItem;

/**
 * An Engine that communicates via an API mimicking the NBoard protocol.
 * <p/>
 * Any command that updates the game or engine state takes a "ping" argument. Callbacks from the Engine
 * that depend on game or engine state take a "pong" argument. The game and engine state are identical
 * if ping==pong.
 */
public abstract class ApiEngine extends ListenerManager<ApiEngine.Listener> {
    public abstract void terminate();

    public abstract void setGame(int ping, COsGame game);

    public abstract void learn();

    public abstract void setContempt(int ping, int contempt);

    public abstract void setMaxDepth(int ping, int maxDepth);

    public abstract void sendMove(int ping, COsMoveListItem mli);

    public abstract void requestHints(int nMoves);

    public abstract void requestMove();

    protected void fireHint(int pong, boolean book, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
        for (ApiEngine.Listener l : getListeners()) {
            l.hint(pong, book, pv, move, eval, nGames, depth, freeformText);
        }
    }

    /**
     * Notify listeners of a status update
     *
     * @param status status text
     */
    protected void fireStatus(int pong, String status) {
        for (Listener l : getListeners()) {
            l.status(pong, status);
        }
    }

    /**
     * Notify listeners that the engine moved
     *
     * @param mli move
     */
    protected void fireEngineMove(int pong, COsMoveListItem mli) {
        for (Listener l : getListeners()) {
            l.engineMove(pong, mli);
        }
    }

    /**
     * Notify listeners that the engine has updated its state
     */
    protected void firePong(int pong) {
        for (Listener l : getListeners()) {
            l.pong(pong);
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
         *
         * @param status status text
         */
        public void status(int pong, String status);

        /**
         * The engine moved.
         * <p/>
         * The engine only sends this message if it relates to the current board position (ping = pong).
         * Otherwise it discards the message.
         *
         * @param mli engine move
         */
        void engineMove(int pong, COsMoveListItem mli);

        /**
         * The engine has updated its internal state
         */
        void pong(int pong);

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
