package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import org.jetbrains.annotations.NotNull;

/**
 */
public class EvalPlayer extends EndgamePlayer {
    private final Eval eval;
    private final int searchDepth;

    public EvalPlayer(Eval eval, int searchDepth) {
        this.eval = eval;
        this.searchDepth = searchDepth;
    }

    @Override public MoveScore calcMove(@NotNull Position board, long moverMoves, int flags) {
        if (board.nEmpty() > 8) {
            return new Search(eval, flags).calcMove(board, board.calcMoves(), searchDepth);
        } else {
            return solveMove(board);
        }
    }

    @Override public String toString() {
        return eval.toString() + ":" + searchDepth;
    }
}

