package com.welty.novello.solver;

import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Position;
import com.welty.novello.core.SampleGames;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class DeepSolverTimer {
    static final List<Position> positions = getPositions(20);

    public static void main(String[] args) {

        final long t0 = System.currentTimeMillis();

        final Solver solver = new Solver();
        run(solver);
        final long dt = System.currentTimeMillis() - t0;
        final long nNodes = solver.nodeCounts.getNNodes();
        System.out.format("%,d ms elapsed; %,d total nodes\n", dt, nNodes);

        System.out.println(solver.nodeCounts.getNodeCountsByDepth());
        System.out.println(solver.hashTable.stats());
    }

    static void run(Solver solver) {
        for (Position position : positions) {
            solver.solve(position.mover(), position.enemy());
        }
    }

    private static List<Position> getPositions(int nEmpty) {
        final List<Position> positions = new ArrayList<>();
        for (MutableGame game : SampleGames.games()) {
            final Position position = game.calcPositionAt(nEmpty);
            if (position==null) {
                throw new IllegalStateException("no position found at " + nEmpty + " empties");
            }
            positions.add(position);
        }
        return positions;
    }
}
