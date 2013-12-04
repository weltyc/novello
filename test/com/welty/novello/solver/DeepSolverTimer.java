package com.welty.novello.solver;

import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Position;
import com.welty.novello.core.SampleGames;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class DeepSolverTimer implements Tunable {
    private static final int nEmpty = 20;
    private static final List<Position> positions = getPositions(nEmpty);

    public static void main(String[] args) {

        final long t0 = System.currentTimeMillis();
        final DeepSolverTimer timer = new DeepSolverTimer();
        timer.run();
        final long dt = System.currentTimeMillis() - t0;
        final long nNodes = timer.nNodes();
        System.out.format("%,d ms elapsed; %,d total nodes\n", dt, nNodes);

//        System.out.println(solver.nodeCounts.getNodeCountsByDepth());
//        System.out.println(solver.hashTable.stats());
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

    private Solver solver;

    @Override public long nNodes() {
        return solver.nodeCounts.getNNodes();
    }

    @Override public void run() {
        this.solver = new Solver();

        for (Position position : positions) {
            solver.solve(position.mover(), position.enemy());
        }
    }
}
