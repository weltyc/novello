package com.welty.othello.api;

import com.welty.othello.gdk.COsGame;

/**
 * Information necessary for an engine to do a search.
 * <p/>
 * This is the shared state in the NBoard protocol.
 */
public class NBoardState {
    /**
     * This should be immutable, but it's not. Don't change it!
     */
    private final COsGame game;

    private final int maxDepth;

    /**
     * Engine contempt, in centidisks
     */
    private final int contempt;

    /**
     * Create a NBoardState whose game is the given game, truncated to moveNumber
     *
     * @param game       game to copy
     * @param moveNumber number of moves to retain from game, 0..game.ml.size()-1
     * @param maxDepth   max search depth
     * @param contempt   board contempt
     */
    public NBoardState(COsGame game, int moveNumber, int maxDepth, int contempt) {
        this.game = new COsGame(game, moveNumber);
        this.maxDepth = maxDepth;
        this.contempt = contempt;
    }

    public NBoardState(COsGame game, int maxDepth, int contempt) {
        this(game, game.nMoves(), maxDepth, contempt);
    }

    public COsGame getGame() {
        return game;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * @return Engine contempt factor, in centi-disks
     */
    public int getContempt() {
        return contempt;
    }
}
