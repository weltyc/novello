package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.novello.solver.Solver;
import com.welty.ntestj.Heights;
import org.jetbrains.annotations.NotNull;

/**
 * A Player that chooses its move using an Eval and a search.
 * <p/>
 * This implementation is NOT thread-safe.
 */
public class EvalPlayer implements Player {
    @NotNull private final Eval eval;
    private final MidgameSearcher.Options midgameOptions;
    private volatile int searchDepth;
    private final String options;
    private final MidgameSearcher searcher;
    private final Solver solver;

    public EvalPlayer(@NotNull Eval eval, int searchDepth, String options) {
        this.eval = eval;
        this.searchDepth = searchDepth;
        this.options = options;
        midgameOptions = new MidgameSearcher.Options(options);
        this.solver = new Solver(eval, midgameOptions);
        this.searcher = solver.midgameSearcher;
        if (midgameOptions.useNtestSearchDepths) {
            System.out.println(new Heights(searchDepth));
        }
    }

    public MoveScore calcMove(@NotNull Position board, long moverMoves, int searchFlags) {
        final int sd = searchDepth;
        if (midgameOptions.useNtestSearchDepths) {
            final Heights heights = new Heights(sd);
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
                    return searcher.getMoveScore(board, board.calcMoves(), sd);
                }
            }
        } else {
            if (board.nEmpty() > 8) {
                return searcher.getMoveScore(board, board.calcMoves(), sd);
            } else {
                return solveMove(board);
            }
        }
    }

    @Override public void setMaxDepth(int maxDepth) {
        this.searchDepth = maxDepth;
    }

    @Override public String toString() {
        return eval + ":" + searchDepth + options;
    }

    /**
     * @param board board to move from
     * @return the perfect-play move
     */
    MoveScore solveMove(Position board) {
        final MoveScore moveScore = solver.getMoveScore(board.mover(), board.enemy());
        return new MoveScore(moveScore.sq, moveScore.score * CoefficientCalculator.DISK_VALUE);
    }

}

