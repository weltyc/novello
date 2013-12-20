package com.welty.novello.solver;

import com.welty.novello.core.Counts;
import com.welty.novello.core.Move;
import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Position;
import com.welty.novello.selfplay.Players;

import java.util.List;

/**
 * Time the midgame search for performance tuning
 */
public class SearchTimer {
    /**
     * Time searches from all positions from 12 games with 10-40 empties (so 360 positions total).
     */
    public static void main(String[] args) {
//        DeepSolverTimer.warmUpHotSpot();

        if (args.length < 2) {
            System.err.println("usage: minDepth maxDepth");
            System.exit(-1);
        }

        final int minDepth = Integer.parseInt(args[0]);
        final int maxDepth = Integer.parseInt(args[1]);

//        countNodes(true, depth, true);

        generateTable(minDepth, maxDepth, false, true);
    }

    private static void generateTable(int minDepth, int maxDepth, boolean doFw, boolean doMpc) {
        final MidgameSearcher fwSearcher = new MidgameSearcher(new Counter(Players.eval("c1s")), "w");
        final MidgameSearcher mpcSearcher = new MidgameSearcher(new Counter(Players.eval("c1s")), "");

        System.out.println();
        System.out.println("depth   k$");
        for (int depth = minDepth; depth <= maxDepth; depth++) {
            final StringBuilder sb = new StringBuilder();
            sb.append(String.format("%2d", depth));
            if (doFw) {
                final long fw = countNodes(false, depth, false, fwSearcher);
                sb.append(String.format(" %,7d", fw / 1000));
            }
            if (doMpc) {
                final long mpc = countNodes(true, depth, false, mpcSearcher);
                sb.append(String.format(" %,7d", mpc / 1000));
            }
            System.out.println(sb);
        }
    }

    private static long countNodes(boolean mpc, int depth, boolean printStats, MidgameSearcher midgameSearcher) {
        midgameSearcher.clear();
        final Counts c0 = midgameSearcher.counts();

        final List<MutableGame> games = SampleGames.saioGames();

        final long t0 = System.currentTimeMillis();
        for (MutableGame game : games) {
            Position pos = game.startPosition;
            for (Move move : game.getMoves()) {
                pos = pos.playOrPass(move.sq);
                final int nEmpty = pos.nEmpty();
                if (nEmpty >= 10 && nEmpty <= 40) {
                    final long moves = pos.calcMoves();
                    if (moves != 0) {
                        midgameSearcher.calcMove(pos, moves, depth);
                        midgameSearcher.clear();
                    }
                }
            }
        }

        final long dt = System.currentTimeMillis() - t0;
        final Counts counts = midgameSearcher.counts().minus(c0);
        if (printStats) {
            final long nEvals = counts.nEvals;
            System.out.format("[%d %3s] %,d ms elapsed. %s. %4.2f us/eval \n"
                    , depth, mpc ? "mpc" : "", dt, counts, dt * 1e3 / nEvals);
        }
        return counts.cost();
    }
}
