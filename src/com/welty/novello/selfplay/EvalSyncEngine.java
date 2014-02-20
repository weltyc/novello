package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.novello.solver.Solver;
import com.welty.ntestj.Heights;
import com.welty.othello.api.AbortCheck;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.protocol.Depth;
import com.welty.othello.protocol.ResponseHandler;
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
        return calcMove(position, maxDepth, new AbortCheck() {
            @Override public boolean shouldAbort() {
                return false;
            }
        });
    }

    @Override public void clear() {
        solver.clear(64);
        searcher.clear();
    }

    @Override public String toString() {
        return eval + ":" + options;
    }

    public MoveScore calcMove(Position position, int maxDepth, AbortCheck abortCheck) {
        final long moverMoves = position.calcMoves();
        if (moverMoves == 0) {
            throw new IllegalArgumentException("Must have a legal move to call calcMove()");
        }
        if (midgameOptions.useNtestSearchDepths && position.nEmpty() <= new Heights(maxDepth).getFullWidthHeight()) {
            // full-width solve
            return solver.getMoveScore(position.mover(), position.enemy(), abortCheck);
        }

        final int depth = calcSearchDepth(position, maxDepth);
        return searcher.getMoveScore(position, moverMoves, depth, abortCheck);
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
     * @param position
     * @param maxDepth
     * @param abortCheck
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
        searcher.calcHints(position, moverMoves, depth, abortCheck, listener);

        if (midgameOptions.useNtestSearchDepths) {
            final int fullWidthHeight = new Heights(maxDepth).getFullWidthHeight();
            if (position.nEmpty() <= fullWidthHeight) {
                // full-width solve
                listener.updateStatus("Solving");
                final MoveScore moveScore = solver.getMoveScore(position.mover(), position.enemy(), abortCheck);
                listener.hint(moveScore, new Depth("100%"));
                listener.updateNodeStats(solver.getCounts().nFlips - n0, System.currentTimeMillis() - t0);
            }
        }
    }

    public interface Listener {
        void updateStatus(String status);
        void updateNodeStats(long nodeCount, long millis);
        void hint(MoveScore moveScore, Depth depth);
    }
}

