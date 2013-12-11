package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.Counter;
import com.welty.novello.solver.Search;
import org.jetbrains.annotations.NotNull;

/**
 */
public class EvalPlayer extends EndgamePlayer {
    private final @NotNull Counter counter;
    private final int searchDepth;
    private final @NotNull String name;
    private final boolean mpc;

    public EvalPlayer(@NotNull Eval eval, int searchDepth, boolean mpc) {
        this.mpc = mpc;
        this.counter = new Counter(eval);
        this.searchDepth = searchDepth;
        this.name = eval + ":" + searchDepth + (mpc?"":"w");
    }

    @Override public MoveScore calcMove(@NotNull Position board, long moverMoves, int searchFlags) {
        if (board.nEmpty() > 8) {
            return new Search(counter, searchFlags).calcMove(board, board.calcMoves(), searchDepth, mpc);
        } else {
            return solveMove(board);
        }
    }

    @Override public String toString() {
        return name;
    }
}

