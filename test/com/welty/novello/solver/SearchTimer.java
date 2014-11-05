/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.solver;

import com.welty.novello.core.Board;
import com.welty.novello.core.Counts;
import com.welty.novello.core.Move8x8;
import com.welty.novello.core.MutableGame;
import com.welty.novello.selfplay.Players;

import java.util.List;

/**
 * Time the midgame search for performance tuning
 */
public class SearchTimer {
    /**
     * Time searches from all positions from 12 games with 10-40 empties (so 372 positions total).
     *
     * Note: the call to midgameSearcher.clear() takes a long time (> 4.4s on this machine) due
     * to synchronization overhead.
     */
    public static void main(String[] args) {
//        DeepSolverTimer.warmUpHotSpot();

        if (args.length < 2) {
            System.err.println("usage: minDepth maxDepth (minEmpty)");
            System.exit(-1);
        }

        final int minDepth = Integer.parseInt(args[0]);
        final int maxDepth = Integer.parseInt(args[1]);
        final int minEmpty = args.length >= 3 ? Integer.parseInt(args[2]) : 10;

//        countNodes(true, depth, true);

        generateTable(minDepth, maxDepth, false, true, true, minEmpty);
    }

    private static void generateTable(int minDepth, int maxDepth, boolean doFw, boolean doMpc, boolean printStats, int minEmpty) {
        final MidgameSearcher fwSearcher = new MidgameSearcher(new Counter(Players.eval("c1s")), "w");
        final MidgameSearcher mpcSearcher = new MidgameSearcher(new Counter(Players.eval("c1s")), "");

        System.out.println();
        System.out.println("depth   k$");
        for (int depth = minDepth; depth <= maxDepth; depth++) {
            final StringBuilder sb = new StringBuilder();
            sb.append(String.format("%2d", depth));
            if (doFw) {
                final long fw = countNodes(false, depth, printStats, fwSearcher, minEmpty);
                sb.append(String.format(" %,7d", fw / 1000));
            }
            if (doMpc) {
                final long mpc = countNodes(true, depth, printStats, mpcSearcher, minEmpty);
                sb.append(String.format(" %,7d", mpc / 1000));
            }
            if (!printStats) {
                System.out.println(sb);
            }
        }
    }

    private static long countNodes(boolean mpc, int depth, boolean printStats, MidgameSearcher midgameSearcher, int minEmpty) {
        midgameSearcher.clear();
        final Counts c0 = midgameSearcher.getCounts();

        final List<MutableGame> games = SampleGames.saioGames();

        final long t0 = System.currentTimeMillis();
        for (MutableGame game : games) {
            Board pos = game.getStartBoard();
            for (Move8x8 move : game.getMlis()) {
                pos = pos.playOrPass(move.getSq());
                final int nEmpty = pos.nEmpty();
                if (nEmpty >= minEmpty && nEmpty <= 40) {
                    final long moves = pos.calcMoves();
                    if (moves != 0) {
                        midgameSearcher.getMoveScore(pos, moves, depth, 0);
                    }
                }
            }
        }

        final long dt = System.currentTimeMillis() - t0;
        final Counts counts = midgameSearcher.getCounts().minus(c0);
        if (printStats) {
            final long nEvals = counts.nEvals;
            System.out.format("[%d %3s] %,d ms elapsed. %s. %4.2f us/eval \n"
                    , depth, mpc ? "mpc" : "", dt, counts, dt * 1e3 / nEvals);
        }
        return counts.cost();
    }
}
