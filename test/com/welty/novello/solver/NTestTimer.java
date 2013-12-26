package com.welty.novello.solver;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.Counts;
import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Position;
import com.welty.novello.selfplay.Players;

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

        timeEndgame(22);         // ntest on i7 2600:  183,355,393 nodes   10.539s = 17.398Mn/s
        timeMidgame(20);         // ntest on i7 2600:   38,926,523 nodes    8.248s =  4.720Mn/s
    }

    private static void timeEndgame(int nEmpty) {
        final Solver solver = new Solver();
        final List<MutableGame> games = SampleGames.saioGames();
        final long t0 = System.currentTimeMillis();
        for (MutableGame game : games) {
            final Position position = game.calcPositionAt(nEmpty);
            if (position == null) {
                throw new IllegalStateException("ntest games file is messed up");
            }
            log.info(solver.getMoveScore(position.mover(), position.enemy()));
        }
        final long dt = System.currentTimeMillis() - t0;
        final double s = dt / 1000.;
        final Counts counts = solver.getCounts();
        log.info(counts);
        final double mn = counts.nFlips * 1e-6;
        log.info(String.format("%3.1f Mn / %3.1f s elapsed = %3.1f Mn/s", mn, s, mn / s));
    }

    private static void timeMidgame(int depth) {
        final MidgameSearcher midgameSearcher = new MidgameSearcher(new Counter(Players.eval("c4s")));
        final List<MutableGame> games = SampleGames.saioGames();
        final long t0 = System.currentTimeMillis();
        for (MutableGame game : games) {
            final Position position = game.calcPositionAt(35);
            if (position == null) {
                throw new IllegalStateException("ntest games file is messed up");
            }
            log.info(midgameSearcher.getMoveScore(position, position.calcMoves(), depth));
        }
        final long dt = System.currentTimeMillis() - t0;
        final double s = dt / 1000.;
        final Counts counts = midgameSearcher.getCounts();
        log.info(counts);
        final double mn = counts.nFlips * 1e-6;
        log.info(String.format("%3.1f Mn / %3.1f s elapsed = %3.1f Mn/s", mn, s, mn / s));
    }
}
