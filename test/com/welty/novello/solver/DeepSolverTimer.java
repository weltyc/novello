package com.welty.novello.solver;

import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Position;
import com.welty.novello.core.SampleGames;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class DeepSolverTimer {
    public static void main(String[] args) {
        final int nEmpty = 20;
        final List<Position> positions = new ArrayList<>();
        for (MutableGame game : SampleGames.games()) {
            final Position position = game.calcPositionAt(nEmpty);
            if (position==null) {
                throw new IllegalStateException("no position found at " + nEmpty + " empties");
            }
            positions.add(position);
        }

        final long t0 = System.currentTimeMillis();

        final Solver solver = new Solver();
        for (Position position : positions) {
            solver.solve(position.mover(), position.enemy());
        }
        final long dt = System.currentTimeMillis() - t0;
        System.out.println(dt + " ms elapsed");

        System.out.println(solver.nodeCounts.getNodeCountsByDepth());
        System.out.println(solver.hashTable.stats());
    }
}
