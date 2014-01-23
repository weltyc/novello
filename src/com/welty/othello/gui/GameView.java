package com.welty.othello.gui;

import com.welty.novello.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Interface for the game that the Viewer is looking at.
 * <p/>
 * All functions that modify this GameView must happen on the Event Dispatch Thread.
 */
public class GameView {
    /**
     * Index of the viewed position in the game (0 = start position, 1 = position after first move or pass)
     */
    private int iPosition;

    private @NotNull MutableGame game;
    /**
     * Engine that is playing black, or null if a human is playing black
     */
    private @Nullable Engine blackEngine;
    /**
     * Engine that is playing white, or null if a human is playing white
     */
    private @Nullable Engine whiteEngine;

    public GameView() {
        game = new MutableGame(Position.START_POSITION, "", "", "");
    }

    /**
     * Get the currently displayed position.
     * <p/>
     * This is not necessarily the game's start position (see {@link #getStartPosition()} or the last position.
     *
     * @return the disks on the board and whether it is white or black's move
     */
    public synchronized @NotNull Position getPosition() {
        return game.getPositionAfter(iPosition);
    }

    /**
     * Get the currently displayed State.
     * <p/>
     * This is not necessarily the game's start state or the last State.
     *
     * @return the disks on the board and whether it is white or black's move
     */
    public synchronized @NotNull State getState() {
        return game.getStateAfter(iPosition);
    }

    /**
     * Move the position pointer to the start of the game
     */
    public synchronized void first() {
        if (iPosition > 0) {
            iPosition = 0;
            fireChange();
        }
    }

    /**
     * Move the position pointer back one move, unless we're at the start of the game.
     */
    public synchronized void prev() {
        if (iPosition > 0) {
            iPosition--;
            fireChange();
        }
    }

    /**
     * Move the position pointer forward one move, unless we're at the end of the game.
     */
    public synchronized void next() {
        if (iPosition < nMoves()) {
            iPosition++;
            fireChange();
        }
    }

    /**
     * Move the position pointer to the end of the game
     */
    public synchronized void last() {
        if (iPosition < nMoves()) {
            iPosition = nMoves();
            fireChange();
        }
    }

    /**
     * @return number of moves in the game, including passes
     */
    public synchronized int nMoves() {
        return game.getMoves().size();
    }

    private final CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Sign up for notifications of Change events.
     *
     * @param listener destination for Change events.
     */
    public void addChangeListener(GameView.ChangeListener listener) {
        // no need to synchronize because listeners is a CopyOnWriteArrayList.
        listeners.add(listener);
    }

    /**
     * Notify all listeners that this has changed
     */
    private void fireChange() {
        // no need to synchronize because listeners is a CopyOnWriteArrayList.
        for (ChangeListener listener : listeners) {
            listener.gameViewChanged();
        }
    }

    /**
     * Set this GameView to the ggf of the given game
     *
     * @param ggf text of the game, in GGF format
     * @throws IllegalArgumentException if the text is not a ggf format game
     */
    public synchronized void setGameGgf(String ggf) {
        game = MutableGame.ofGgf(ggf);
        iPosition = 0;
        ++ping;
        fireChange();
    }

    public synchronized int getIPosition() {
        return iPosition;
    }

    public synchronized void setIPosition(int IPosition) {
        this.iPosition = IPosition;
        fireChange();
    }

    public synchronized String getBlackName() {
        return game.blackName;
    }

    public synchronized String getWhiteName() {
        return game.whiteName;
    }

    public synchronized List<Move8x8> getMoves() {
        return new ArrayList<>(game.getMoves());
    }

    public synchronized Position getStartPosition() {
        return game.getStartPosition();
    }

    private long ping;

    /**
     * Time the last move request was made.
     * <p/>
     * This is used for both human and computer players.
     */
    private long moveRequestTime;

    /**
     * Start a game.
     * <p/>
     * Set the board position to the start position. Request moves from engines as appropriate.
     * <p/>
     * Shuts down previous engine.
     *
     * @param blackEngine Engine playing Black or null if a human player
     * @param whiteEngine Engine playing White or null if a human player
     */
    public void newGame(@Nullable Engine blackEngine, @Nullable Engine whiteEngine) {
        newGame(blackEngine, whiteEngine, Position.START_POSITION);
    }


    /**
     * Start a game.
     * <p/>
     * Set the board position to the start position. Request moves from engines as appropriate.
     *
     * @param blackEngine   Engine playing Black
     * @param whiteEngine   Engine playing White
     * @param startPosition start position for the game
     */
    public synchronized void newGame(@Nullable Engine blackEngine, @Nullable Engine whiteEngine, Position startPosition) {
        this.blackEngine = blackEngine;
        this.whiteEngine = whiteEngine;
        game = new MutableGame(startPosition, calcName(blackEngine), calcName(whiteEngine), NovelloUtils.getHostName());
        iPosition = 0;
        ++ping;
        requestMove();
        fireChange();
    }

    /**
     * If the player to move at the LastPosition is an Engine, call its requestMove() function
     */
    private void requestMove() {
        if (!isOver()) {
            final Engine engine = currentEngine();
            // set moveRequestTime regardless of whether mover is a Human or an Engine
            moveRequestTime = System.currentTimeMillis();
            if (engine != null) {
                engine.requestMove(this, getPosition(), ping);
            }
        }
    }

    private @NotNull String calcName(Engine engine) {
        if (engine == null) {
            // a human player!
            return NovelloUtils.getUserName();
        } else {
            return engine.getName();
        }
    }

    /**
     * Update the game.
     * <p/>
     * If the ping does not match the current state of this GameView, the move is considered outdated and thus ignored.
     * <p/>
     * Like all functions that modify this GameView, this function should only be called from the Event Dispatch Thread.
     *
     * @param moveScore move and Engine score of the move
     * @param ping      ping from Engine.requestMove()
     */
    public synchronized void engineMove(@NotNull MoveScore moveScore, long ping) {
        if (ping == this.ping) {
            final long dt = System.currentTimeMillis() - moveRequestTime;
            game.play(moveScore, dt * 0.001);
            ++ping;
            iPosition = nMoves();
            requestMove();
            fireChange();
        }
    }

    /**
     * The human has clicked on the board.
     * <p/>
     * If not viewing the last position, move the game pointer forward.
     * If it's the human player's move and we're currently viewing the last position, update the game.
     *
     * @param sq The square the human clicked on.
     */
    public synchronized void boardClick(int sq) {
        if (getIPosition() < nMoves()) {
            iPosition++;
            fireChange();
        } else if (isHumansMove()) {
            final long legalMoves = getPosition().calcMoves();
            if (legalMoves == 0) {
                ++ping;
                game.pass();
            } else if (BitBoardUtils.isBitSet(legalMoves, sq)) {
                final long dt = System.currentTimeMillis() - moveRequestTime;
                ++ping;
                game.play(new MoveScore(sq, 0), dt * 0.001);
            } else {
                // Human to move, but clicked on a square that is not a legal move. Ignore.
                return;
            }
            iPosition++;
            requestMove();
            fireChange();
        }
    }

    private boolean isHumansMove() {
        return currentEngine() == null && !isOver();
    }

    /**
     * The engine to play at the LastPosition
     * <p/>
     * This looks only at colour; if the game is over at the LastPosition this will still return a value.
     * Call isOver() if you want to know whether the game is over.
     *
     * @return engine or null if the human's move is at the last position.
     */
    private @Nullable Engine currentEngine() {
        return game.getLastPosition().blackToMove ? blackEngine : whiteEngine;
    }

    private boolean isOver() {
        final Position pos = game.getLastPosition();
        return pos.calcMoves() == 0 && pos.enemyMoves() == 0;

    }

    interface ChangeListener {
        /**
         * Do whatever processing is necessary when the game view changes
         */
        void gameViewChanged();
    }
}
