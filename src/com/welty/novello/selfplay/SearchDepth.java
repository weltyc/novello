package com.welty.novello.selfplay;

import com.orbanova.common.feed.Feed;
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
    final boolean solve;
    final int depth;
    final int probableSolveDepth;

    SearchDepth(boolean solve, int depth, int probableSolveDepth) {
        this.solve = solve;
        this.depth = depth;
        this.probableSolveDepth = probableSolveDepth;
    }

    public boolean isFullSolve() {
        return solve;
    }

    /**
     * @return true if this solve is either a probable solve or a full-width solve
     */
    public boolean isProbableSolve() {
        return depth >= probableSolveDepth;
    }

    public String humanString() {
        if (solve) {
            return "100%";
        } else if (depth == probableSolveDepth) {
            return "60%";
        } else {
            return depth + " ply";
        }
    }

    /**
     * @return the next SearchDepth to search after this one, or null if this is a full-width solve.
     */
    SearchDepth next() {
        if (depth < probableSolveDepth) {
            return new SearchDepth(false, depth + 1, probableSolveDepth);
        } else if (solve) {
            return null;
        } else {
            return new SearchDepth(true, depth, probableSolveDepth);
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

        final int maxDepth = nEmpty <= probableSolveNEmpty ? nEmpty - solverStart : maxMidgameDepth;
        final boolean solve = nEmpty <= solveNEmpty;

        return new SearchDepth(solve, maxDepth, nEmpty - solverStart);
    }

    public Depth displayDepth() {
        if (solve) {
            return new Depth("100%");
        } else if (isProbableSolve()) {
            return new Depth("60%");
        } else {
            return new Depth(depth);
        }
    }

    public SearchDepth startDepth() {
        if (depth > 1) {
            return new SearchDepth(false, 1, probableSolveDepth);
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
