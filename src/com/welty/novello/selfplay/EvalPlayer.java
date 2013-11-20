package com.welty.novello.selfplay;

import com.welty.novello.eval.EvalStrategy;
import com.welty.novello.eval.StrategyBasedEval;
import com.welty.novello.solver.BitBoard;
import org.jetbrains.annotations.NotNull;

/**
 */
public class EvalPlayer extends EndgamePlayer {
    final Eval eval;

    EvalPlayer(Eval eval) {
        this.eval = eval;
    }

    public EvalPlayer(EvalStrategy strategy) {
        this(new StrategyBasedEval(strategy));
    }

    @Override public int calcMove(@NotNull BitBoard board) {
        if (board.nEmpty() > 8) {
            long moves = board.calcMoves();
            int bestScore = Integer.MIN_VALUE;
            int bestSq = -1;
            while (moves != 0) {
                final int sq = Long.numberOfTrailingZeros(moves);
                moves &= moves - 1;
                final BitBoard subBoard = board.play(sq);
                final int moveScore = -eval.eval(subBoard.mover(), subBoard.enemy());
                if (moveScore >= bestScore) {
                    bestScore = moveScore;
                    bestSq = sq;
                }
            }
            return bestSq;
        } else {
            return solveMove(board);
        }
    }
}
