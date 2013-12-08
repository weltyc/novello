package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.core.Position;
import com.welty.novello.solver.Solver;
import org.jetbrains.annotations.NotNull;

/**
 * Play randomly except play perfectly with <= 8 empties
 */
public class EndgamePlayer implements Player {
    private static final ThreadLocal<Solver> solvers = new ThreadLocal<Solver>() {
        @Override protected Solver initialValue() {
            return new Solver();
        }
    };

    @Override public MoveScore calcMove(@NotNull Position board, long moverMoves, int searchFlags) {
        return solveMove(board);
    }

    /**
     * @param board board to move from
     * @return the perfect-play move
     */
    MoveScore solveMove(Position board) {
        final MoveScore moveScore = solvers.get().solveWithMove(board.mover(), board.enemy());
        return new MoveScore(moveScore.sq, moveScore.score * CoefficientCalculator.DISK_VALUE);
    }

    @Override public String toString() {
        return this.getClass().getSimpleName();
    }
}
