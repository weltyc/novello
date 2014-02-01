package com.welty.othello.api;

import com.orbanova.common.misc.ListenerManager;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveListItem;

/**
 * An Engine that communicates via an API mimicking the NBoard protocol
 */
public abstract class ApiEngine extends ListenerManager<ApiEngine.Listener> {

    public abstract void ping();

    public abstract void terminate();

    public abstract void setGame(COsGame game);

    public abstract void learn();

    public abstract void setContempt(int contempt);

    public abstract void sendMove(COsMoveListItem mli);

    public abstract void requestHints(int nMoves);

    public abstract void requestMove();

    protected void fireHint(boolean book, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
        for (ApiEngine.Listener l : getListeners()) {
            l.hint(book, pv, move, eval, nGames, depth, freeformText);
        }
    }

    /**
     * Notify listeners of a status update
     *
     * @param status status text
     */
    protected void fireStatus(String status) {
        for (Listener l : getListeners()) {
            l.status(status);
        }
    }

    /**
     * Notify listeners that the engine moved
     *
     * @param mli move
     */
    protected void fireEngineMove(COsMoveListItem mli) {
        for (Listener l : getListeners()) {
            l.engineMove(mli);
        }
    }

    /**
     * Notify listeners that the engine is ready to accept commands
     */
    protected void fireEngineReady() {
        for (Listener l : getListeners()) {
            l.engineReady();
        }
    }


    protected void fireParseError(String command, String errorMessage) {
        for (Listener l : getListeners()) {
            l.parseError(command, errorMessage);
        }
    }

    /**
     * Listens to responses from the Engine
     */
    public interface Listener {
        /**
         * The Engine updated its status
         *
         * @param status status text
         */
        public void status(String status);

        /**
         * The engine moved.
         * <p/>
         * The engine only sends this message if it relates to the current board position (ping = pong).
         * Otherwise it discards the message.
         *
         * @param mli engine move
         */
        void engineMove(COsMoveListItem mli);

        /**
         * The engine is ready to accept commands (ping=pong).
         */
        void engineReady();

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
        void hint(boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText);

        /**
         * The engine sent a message which appears to be an nboard protocol message but can't be parsed correctly.
         *
         * @param command      command from engine
         * @param errorMessage error message from parser
         */
        void parseError(String command, String errorMessage);
    }
}
