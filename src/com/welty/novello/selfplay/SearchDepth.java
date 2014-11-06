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

package com.welty.novello.selfplay;

import com.welty.novello.eval.Mpc;
import com.welty.othello.protocol.Depth;
import lombok.EqualsAndHashCode;

/**
 */
@EqualsAndHashCode
public final class SearchDepth {
    final int depth;
    /**
     * MPC cut width index, or Integer.MAX_VALUE if MPC is not used.
     */
    final int width;

    /**
     * If true, this is a (probable or full) solve
     */
    final boolean isSolve;

    /**
     * @param depth              search depth, in ply
     * @param width              search width index, or Integer.MAX_VALUE for a full-width search
     * @param probableSolveDepth depth at which a search is a probable solve
     */
    SearchDepth(int depth, int width, int probableSolveDepth) {
        this(depth, width, depth >= probableSolveDepth);
    }

    /**
     * @param depth   search depth, in ply
     * @param width   search width index, or Integer.MAX_VALUE for a full-width search
     * @param isSolve true if this is a (probable or full) solve
     */
    SearchDepth(int depth, int width, boolean isSolve) {
        this.depth = depth;
        this.width = width;
        this.isSolve = isSolve;
    }

    /**
     * @return true if this is a full-width, non-selective solve.
     */
    public boolean isFullSolve() {
        return width == Integer.MAX_VALUE;
    }

    /**
     * @return true if this solve is either a probable solve or a full-width solve
     */
    public boolean isSolve() {
        return isSolve;
    }

    @Override public String toString() {
        if (isFullSolve()) {
            return "100%";
        } else if (isSolve) {
            return Mpc.widthString(width);
        } else {
            return depth + " ply";
        }
    }

    public Depth displayDepth() {
        if (isFullSolve()) {
            return new Depth("100%");
        } else if (isSolve()) {
            return new Depth(toString());
        } else {
            return new Depth(depth);
        }
    }

    /**
     *
     * @return midgame search depth, in plies
     */
    public int getDepth() {
        return depth;
    }

    /**
     * @return midgame MPC width index
     */
    public int getWidth() {
        return width;
    }
}
