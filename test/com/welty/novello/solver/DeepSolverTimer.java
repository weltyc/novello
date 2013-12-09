package com.welty.novello.solver;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 */
public class DeepSolverTimer implements Tunable {
    private final List<Position> positions;
    private final boolean logResults;
    private static final Logger log = Logger.logger(DeepSolverTimer.class);

    public DeepSolverTimer(int nEmpty) {
        this(nEmpty, false);
    }

    public DeepSolverTimer(int nEmpty, boolean logResults) {
        this.logResults = logResults;
        positions = getPositions(nEmpty);
    }

    public static void main(String[] args) {
        // warm up Hot Spot
        new DeepSolverTimer(20).run();

//        System.out.println(Typical.timing(new DeepSolverTimer(20)));

        final DeepSolverTimer timer = new DeepSolverTimer(24, true);
        timer.runMultithreaded();
//        final long t0 = System.currentTimeMillis();
//        final DeepSolverTimer timer = new DeepSolverTimer(24, true);
//        timer.run();
//        final long dt = System.currentTimeMillis() - t0;
//        final long nNodes = timer.nNodes();
//        System.out.format("%,d ms elapsed; %,d total nodes\n", dt, nNodes);
//        timer.solver.dumpStatistics();
//        System.out.println(timer.solver.nodeCounts.getNodeCountsByDepth());
//        System.out.println(timer.solver.hashTables.stats());
    }

    private static List<Position> getPositions(int nEmpty) {
        final List<Position> positions = new ArrayList<>();
        for (MutableGame game : SampleGames.games()) {
            final Position position = game.calcPositionAt(nEmpty);
            if (position == null) {
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
            final int score = solver.solve(position.mover(), position.enemy());
            if (logResults) {
                log.info("score : " + score);
            }
        }
    }

    public void runMultithreaded() {
        final ExecutorService executorService = Executors.newFixedThreadPool(8);
        final ArrayList<Future<Long>> futures = new ArrayList<>();

        // run the positions from most complicated to least complicated so we get done sooner
        final int[] positionOrder = {8, 6, 10, 2, 7, 1, 11, 4, 3, 5, 9, 0};

        for (int i = 0; i < positions.size(); i++) {
            final int j = positionOrder[i];
            final SolveTask task = new SolveTask(positions.get(j), j);
            futures.add(executorService.submit(task));
        }
        executorService.shutdown();
        long nNodes = 0;
        for (Future<Long> future : futures) {
            try {
                nNodes += future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        log.info(String.format("Total node count : %,d kn", nNodes / 1000));
    }

    private class SolveTask implements Callable<Long> {
        private final Position position;
        private final int i;

        public SolveTask(Position position, int i) {
            this.position = position;
            this.i = i;
        }

        @Override public Long call() throws Exception {
            final Solver solver = new Solver();
            final int score = solver.solve(position.mover(), position.enemy());
            final long nNodes = solver.nodeCounts.getNNodes();
            if (logResults) {
                final String msg = String.format("position %2d:   score %d   %,6d kn", i, score, nNodes / 1000);
                log.info(msg);
            }
            return nNodes;
        }
    }
}
