package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.novello.solver.Solver;
import com.welty.ntestj.Heights;
import com.welty.othello.api.AbortCheck;
import com.welty.othello.protocol.Depth;
import org.jetbrains.annotations.NotNull;

/**
 * A SyncEngine that chooses its move using an Eval and a search.
 * <p/>
 * This implementation is NOT thread-safe.
 */
public class EvalSyncEngine implements SyncEngine {
    @NotNull private final Eval eval;
    private final MidgameSearcher.Options midgameOptions;
    private final String options;
    private final MidgameSearcher searcher;
    private final Solver solver;

    public EvalSyncEngine(@NotNull Eval eval, String options) {
        this.eval = eval;
        this.options = options;
        midgameOptions = new MidgameSearcher.Options(options);
        this.solver = new Solver(eval, midgameOptions);
        this.searcher = solver.midgameSearcher;
    }

    public MoveScore calcMove(@NotNull Position position, int maxDepth) {
        return calcMove(position, maxDepth, AbortCheck.NEVER, Listener.NULL);
    }

    @Override public void clear() {
        solver.clear(64);
        searcher.clear();
    }

    @Override public String toString() {
        return eval + ":" + options;
    }

    public MoveScore calcMove(Position position, int maxDepth, AbortCheck abortCheck, Listener listener) {
        final long moverMoves = position.calcMoves();
        if (moverMoves == 0) {
            throw new IllegalArgumentException("Must have a legal move to call calcMove()");
        }
        final long n0 = searcher.getCounts().nFlips;
        final long t0 = System.currentTimeMillis();

        final MoveScore result;
        if (shouldSolve(position, maxDepth)) {
            listener.updateStatus("Solving...");
            // full-width solve
            result = solver.getMoveScore(position.mover(), position.enemy(), abortCheck);
        } else {
            final int depth = calcSearchDepth(position, maxDepth);
            final Depth displayDepth = calcDisplayDepth(position, depth);
            listener.updateStatus("Searching at " + displayDepth);
            result = searcher.getMoveScore(position, moverMoves, depth, abortCheck);
        }
        listener.updateNodeStats(searcher.getCounts().nFlips- n0, System.currentTimeMillis() - t0);
        return result;
    }

    final int calcSearchDepth(Position position, int maxDepth) {
        if (midgameOptions.useNtestSearchDepths) {
            final Heights heights = new Heights(maxDepth);
            if (position.nEmpty() <= heights.getProbableSolveHeight()) {
                // probable solve
                final int solverStart = midgameOptions.useSolver ? MidgameSearcher.SOLVER_START_DEPTH - 1 : 0;
                return position.nEmpty() - solverStart;
            }
        }
        return maxDepth;
    }

    /**
     * Only called if there is at least one legal move from this position
     *
     * @param listener listener for intermediate results
     */
    public void calcHints(Position position, int maxDepth, AbortCheck abortCheck, Listener listener) {
        final long moverMoves = position.calcMoves();
        if (moverMoves == 0) {
            throw new IllegalArgumentException("Must have a legal move to call calcMove()");
        }
        final long n0 = searcher.getCounts().nFlips;
        final long t0 = System.currentTimeMillis();
        final int depth = calcSearchDepth(position, maxDepth);

        for (int i = 1; i <= depth; i++) {
            final Depth displayDepth = calcDisplayDepth(position, i);
            listener.updateStatus("Searching at " + displayDepth);
            final MoveScore moveScore1 = searcher.getMoveScore(position, moverMoves, i, abortCheck);
            listener.hint(moveScore1, displayDepth);
            listener.updateNodeStats(searcher.getCounts().nFlips - n0, System.currentTimeMillis() - t0);
        }

        if (shouldSolve(position, maxDepth)) {
            // full-width solve
            listener.updateStatus("Solving");
            final MoveScore moveScore = solver.getMoveScore(position.mover(), position.enemy(), abortCheck);
            listener.hint(moveScore, new Depth("100%"));
            listener.updateNodeStats(solver.getCounts().nFlips - n0, System.currentTimeMillis() - t0);
        }
    }

    private static Depth calcDisplayDepth(Position position, int i) {
        final Depth displayDepth;
        if (i + MidgameSearcher.SOLVER_START_DEPTH > position.nEmpty()) {
            displayDepth = new Depth("60%");
        } else {
            displayDepth = new Depth(i);
        }
        return displayDepth;
    }

    private boolean shouldSolve(Position position, int maxDepth) {
        return midgameOptions.useNtestSearchDepths && position.nEmpty() <= new Heights(maxDepth).getFullWidthHeight();
    }

    public interface Listener {
        Listener NULL = new Listener() {
            @Override public void updateStatus(String status) {
            }

            @Override public void updateNodeStats(long nodeCount, long millis) {
            }

            @Override public void hint(MoveScore moveScore, Depth depth) {
            }
        };

        void updateStatus(String status);

        void updateNodeStats(long nodeCount, long millis);

        void hint(MoveScore moveScore, Depth depth);
    }
}

