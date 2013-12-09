package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.CountingEval;
import com.welty.novello.solver.Search;
import org.jetbrains.annotations.NotNull;

/**
 */
public class EvalPlayer extends EndgamePlayer {
    private final @NotNull CountingEval eval;
    private final int searchDepth;

    public EvalPlayer(@NotNull Eval eval, int searchDepth) {
        this.eval = new CountingEval(eval);
        this.searchDepth = searchDepth;
    }

    @Override public MoveScore calcMove(@NotNull Position board, long moverMoves, int searchFlags) {
        if (board.nEmpty() > 8) {
            return new Search(eval, searchFlags).calcMove(board, board.calcMoves(), searchDepth);
        } else {
            return solveMove(board);
        }
    }

    @Override public String toString() {
        return eval.toString() + ":" + searchDepth;
    }
}

