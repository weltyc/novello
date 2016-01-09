/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.external.api;

import com.welty.othello.gdk.COsGame;

/**
 * Information necessary for an engine to do a search.
 * <p/>
 * This class is immutable. It is a snapshot of the shared state in the NBoard protocol.
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

    public int getMaxMidgameDepth() {
        return maxDepth;
    }

    /**
     * @return Engine contempt factor, in centi-disks
     */
    public int getContempt() {
        return contempt;
    }
}
