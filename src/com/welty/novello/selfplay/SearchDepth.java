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

import com.orbanova.common.feed.Feed;
import com.welty.novello.eval.Mpc;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.ntestj.Heights;
import com.welty.othello.protocol.Depth;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

/**
 */
@EqualsAndHashCode @ToString
final class SearchDepth {
    final int depth;
    /**
     * MPC cut width index, or Integer.MAX_VALUE if MPC is not used.
     */
    final int width;
    final int probableSolveDepth;

    /**
     *
     * @param depth search depth, in ply
     * @param width search width index, or Integer.MAX_VALUE for a full-width search
     * @param probableSolveDepth depth at which a search is a probable solve
     */
    SearchDepth(int depth, int width, int probableSolveDepth) {
        this.depth = depth;
        this.width = width;
        this.probableSolveDepth = probableSolveDepth;
    }

    public boolean isFullSolve() {
        return width==Integer.MAX_VALUE;
    }

    /**
     * @return true if this solve is either a probable solve or a full-width solve
     */
    public boolean isProbableSolve() {
        return depth >= probableSolveDepth;
    }

    public String humanString() {
        if (isFullSolve()) {
            return "100%";
        } else if (depth == probableSolveDepth) {
            return Mpc.widthString(width);
        } else {
            return depth + " ply";
        }
    }

    /**
     * @return the next SearchDepth to search after this one, or null if this is a full-width solve.
     */
    SearchDepth next() {
        if (depth < probableSolveDepth) {
            return new SearchDepth(depth + 1, width, probableSolveDepth);
        } else if (isFullSolve()) {
            return null;
        } else {
            final int nextWidth = width < Mpc.maxWidth() ? width + 1 : Integer.MAX_VALUE;
            return new SearchDepth(depth, nextWidth, probableSolveDepth);
        }
    }

    /**
     * Calculate the maximum search depth
     *
     * @return max SearchDepth
     */
    public static SearchDepth maxDepth(int nEmpty, int maxMidgameDepth, MidgameSearcher.Options midgameOptions) {
        final int solverStart = midgameOptions.variableEndgame ? MidgameSearcher.SOLVER_START_DEPTH - 1 : 0;

        final int probableSolveNEmpty;
        final int solveNEmpty;

        if (midgameOptions.variableEndgame) {
            final Heights heights = new Heights(maxMidgameDepth);
            probableSolveNEmpty = heights.getProbableSolveHeight();
            solveNEmpty = heights.getFullWidthHeight();
        } else {
            probableSolveNEmpty = maxMidgameDepth - solverStart;
            solveNEmpty = 0;
        }

        final int solveDepth = nEmpty - solverStart;
        final int maxDepth;
        final int maxWidth;

        if (nEmpty <= solveNEmpty) {
            maxDepth = solveDepth;
            maxWidth = Integer.MAX_VALUE;
        } else if (nEmpty <= probableSolveNEmpty) {
            maxDepth = solveDepth;
            maxWidth = 1;
        } else {
            maxDepth = maxMidgameDepth;
            maxWidth = 0;
        }
        return new SearchDepth(maxDepth, maxWidth, nEmpty - solverStart);
    }

    public Depth displayDepth() {
        if (isFullSolve()) {
            return new Depth("100%");
        } else if (isProbableSolve()) {
            return new Depth(humanString());
        } else {
            return new Depth(depth);
        }
    }

    public SearchDepth startDepth() {
        if (depth > 1) {
            return new SearchDepth(1, 0, probableSolveDepth);
        } else {
            return this;
        }
    }

    /**
     * Create a Feed that goes from the simplest search to this search
     * @return the Feed
     */
    public Feed<SearchDepth> depthFeed() {
        return new SearchDepthFeed(this);
    }

    private static class SearchDepthFeed extends Feed<SearchDepth> {
        private final SearchDepth max;
        private SearchDepth last = null;

        public SearchDepthFeed(SearchDepth max) {
            super();
            this.max = max;
        }

        @Nullable @Override public SearchDepth next() {
            if (last==null) {
                last = max.startDepth();
            }  else if (last.equals(max)) {
                last = null;
            } else {
                last = last.next();
            }
            return last;
        }
    }
}
