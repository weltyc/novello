package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.novello.solver.Solver;
import org.jetbrains.annotations.NotNull;

/**
 * A Player that chooses its move using an Eval and a search.
 * <p/>
 * This implementation is NOT thread-safe.
 */
public class EvalPlayer implements Player {
    @NotNull private final Eval eval;
    private volatile int searchDepth;
    private final String options;
    private final MidgameSearcher searcher;
    private final Solver solver;

    public EvalPlayer(@NotNull Eval eval, int searchDepth, String options) {
        this.eval = eval;
        this.searchDepth = searchDepth;
        this.options = options;
        this.solver = new Solver(eval, new MidgameSearcher.Options(options));
        this.searcher = solver.midgameSearcher;
    }

    public MoveScore calcMove(@NotNull Position board, long moverMoves, int searchFlags) {
        if (board.nEmpty() > 8) {
            return searcher.getMoveScore(board, board.calcMoves(), searchDepth);
        } else {
            return solveMove(board);
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

