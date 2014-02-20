package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.novello.solver.Solver;
import com.welty.ntestj.Heights;
import com.welty.othello.api.AbortCheck;
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
     * @param pong
     * @param responseHandler
     */
    public void calcHints(Position position, int maxDepth, AbortCheck abortCheck, int pong, ResponseHandler responseHandler) {
        final long moverMoves = position.calcMoves();
        if (moverMoves == 0) {
            throw new IllegalArgumentException("Must have a legal move to call calcMove()");
        }
        final int depth = calcSearchDepth(position, maxDepth);
        searcher.calcHints(position, moverMoves, depth, abortCheck, pong, responseHandler);

        if (midgameOptions.useNtestSearchDepths && position.nEmpty() <= new Heights(maxDepth).getFullWidthHeight()) {
            // full-width solve
            final MoveScore moveScore = solver.getMoveScore(position.mover(), position.enemy(), abortCheck);
            responseHandler.handle(moveScore.toHintResponse(pong, new Depth("100%")));
        }
    }
}

