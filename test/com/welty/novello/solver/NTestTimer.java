package com.welty.novello.solver;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.Counts;
import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Position;

import java.util.List;

/**
 */
public class NTestTimer {
    private static final Logger log = Logger.logger(NTestTimer.class);

    /**
     * Run an endgame timing test with the positions from Ntest's built-in timer
     */
    public static void main(String[] args) {
        DeepSolverTimer.warmUpHotSpot();

        final Solver solver = new Solver();
        final List<MutableGame> games = SampleGames.saioGames();
        final long t0 = System.currentTimeMillis();
        for (MutableGame game : games) {
            final Position position = game.calcPositionAt(22);
            log.info(solver.solveWithMove(position.mover(), position.enemy()));
        }
        final long dt = System.currentTimeMillis() - t0;
        final double s = dt/1000.;
        final Counts counts = solver.getCounts();
        log.info(counts);
        final double mn = counts.nFlips * 1e-6;
        log.info(String.format("%3.1f Mn / %3.1f s elapsed = %3.1f Mn/s", s, mn, mn/s));
    }
}
