package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.novello.solver.Solver;
import com.welty.ntestj.Heights;
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

    public MoveScore calcMove(@NotNull Position board, int maxDepth) {
        if (midgameOptions.useNtestSearchDepths) {
            final Heights heights = new Heights(maxDepth);
            if (board.nEmpty() <= heights.getFullWidthHeight()) {
                // full-width solve
                return solveMove(board);
            } else {
                if (board.nEmpty() <= heights.getProbableSolveHeight()) {
                    // probable solve
                    final int solverStart = midgameOptions.useSolver ? MidgameSearcher.SOLVER_START_DEPTH - 1 : 0;
                    final int depth = board.nEmpty() - solverStart;
                    return searcher.getMoveScore(board, board.calcMoves(), depth);
                } else {
                    return searcher.getMoveScore(board, board.calcMoves(), maxDepth);
                }
            }
        } else {
            return searcher.getMoveScore(board, board.calcMoves(), maxDepth);
        }
    }

    @Override public void clear() {
        solver.clear(64);
        searcher.clear();
    }

    @Override public String toString() {
        return eval + ":" + options;
    }

    /**
     * @param board board to move from
     * @return the perfect-play move
     */
    MoveScore solveMove(Position board) {
        return solver.getMoveScore(board.mover(), board.enemy());
    }
}

