package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.Counter;
import com.welty.novello.solver.MidgameSearcher;
import org.jetbrains.annotations.NotNull;

/**
 */
public class EvalPlayer extends EndgamePlayer {
    private final int searchDepth;
    private final @NotNull String name;
    private final MidgameSearcher  searcher;

    public EvalPlayer(@NotNull Eval eval, int searchDepth, String options) {
        this.searchDepth = searchDepth;
        this.name = eval + ":" + searchDepth + options;
        this.searcher = new MidgameSearcher(new Counter(eval), options);
    }

    @Override public MoveScore calcMove(@NotNull Position board, long moverMoves, int searchFlags) {
        if (board.nEmpty() > 8) {
            return searcher.calcMove(board, board.calcMoves(), searchDepth);
        } else {
            return solveMove(board);
        }
    }

    @Override public String toString() {
        return name;
    }
}

