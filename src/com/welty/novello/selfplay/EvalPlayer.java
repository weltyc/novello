package com.welty.novello.selfplay;

import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.EvalStrategy;
import com.welty.novello.eval.StrategyBasedEval;
import com.welty.novello.solver.BitBoard;
import com.welty.novello.solver.BitBoardUtils;
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
                final int moveScore = -score(subBoard);
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

    private int score(BitBoard subBoard) {
        final long mover = subBoard.mover();
        final long enemy = subBoard.enemy();
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(mover, enemy);
        final int moveScore;
        if (moverMoves!=0) {
            moveScore = eval.eval(mover, enemy, moverMoves, enemyMoves);
        }
        else if (enemyMoves !=0) {
            moveScore = -eval.eval(enemy, mover, enemyMoves, moverMoves);
        }
        else {
            moveScore = CoefficientCalculator.DISK_VALUE*(Long.bitCount(mover)-Long.bitCount(enemy));
        }
        return moveScore;
    }

    @Override public String toString() {
        return eval.toString();
    }
}
