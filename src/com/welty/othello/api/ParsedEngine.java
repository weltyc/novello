package com.welty.othello.api;

import com.welty.othello.c.CReader;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;

/**
 * Class that controls communication with an engine.
 */
public class ParsedEngine extends StatelessEngine implements NBoardEngine.Listener {
    private volatile int lastPing;
    private volatile int m_pong;
    private String name;
    private final @NotNull NBoardEngine engine;
    private volatile @NotNull String status = "";

    /**
     * @param engine command-line engine
     */
    public ParsedEngine(@NotNull final NBoardEngine engine) {
        this.engine = engine;
        this.engine.sendCommand("nboard 1");
        name = engine.toString();
        engine.addListener(this);
    }

    @NotNull public String getName() {
        return name;
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
        return status;
    }

    @Override public boolean isReady() {
        return m_pong >= lastPing;
    }

    private void setName(String name) {
        this.name = name;
    }


    private void sendPing(int ping) {
        engine.sendCommand("ping " + ping);
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
            sendPing(lastPing);
        }
    }

    /**
     * Parse a command received from the engine and notify listeners.
     * <p/>
     * The "command" is the first word of the response.
     * <p/>
     * Listeners are notified of all commands regardless of whether ping is up to date.
     * <p/>
     * Blank lines are ignored.
     * <p/>
     * Unknown commands are ignored.
     * <p/>
     * Commands that are known but the rest of the line is in an incorrect format result
     * in listeners receiving parseError().
     */
    @Override public synchronized void onMessageReceived(String message) {
        final CReader is = new CReader(message);
        String sCommand = is.readString();
        is.ignoreWhitespace();

        try {
            switch (sCommand) {
                case "pong":
                    m_pong = is.readInt();
                    if (isReady()) {
                        fireEngineReady(m_pong);
                    }
                    break;
                case "status":
                    // the engine is busy and is telling the user why
                    setStatus(is.readLine());
                    break;
                case "set":
                    String variable = is.readString();
                    if (variable.equals("myname")) {
                        String sName = is.readString();
                        setName(sName);
                    }
                    break;

                // For commands from here on, the receiver should only use these commands if the computer is up-to-date
                // but we don't verify that here - the caller now verifies that (because of multiple engines).
                case "===":
                    setStatus("");
                    // now update the move list

                    // Edax produces the mli with spaces between components rather than slashes.
                    // Translate to normal form if there are spaces.
                    final String mliText = is.readLine().trim().replaceAll("\\s+", "/");
                    final OsMoveListItem mli = new OsMoveListItem(mliText);

                    fireEngineMove(m_pong, mli);
                    break;
                case "book":
                case "search":
                    // computer giving hints
                    // search [pv] [eval] 0         [depth] [freeform text]
                    // book   [pv] [eval] [# games] [depth] [freeform text]
                    final boolean isBook = sCommand.equals("book");

                    final String pv = is.readString();
                    final CMove move;
                    try {
                        move = new CMove(pv.substring(0, 2));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Can't create move from first two characters of pv (" + pv + ")");
                    }
                    final String eval = is.readString();
                    final int nGames = is.readInt();
                    final String depth = is.readString();
                    final String freeformText = is.readLine();
                    fireHint(m_pong, isBook, pv, move, eval, nGames, depth, freeformText);
                    break;
                case "learn":
                    setStatus("");
                    break;
            }
        } catch (EOFException | IllegalArgumentException e) {
            fireParseError(m_pong, message, e.toString());
        }
    }

    @Override public void onEngineTerminated() {
        setStatus("The engine (" + name + ") has terminated.");
    }

    private void setStatus(@NotNull String status) {
        this.status = status;
        fireStatusChanged();
    }
}
