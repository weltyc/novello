package com.welty.novello.solver;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.PositionValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 */
public class DeepSolverTimer implements Tunable {
    private final List<PositionValue> pvs;
    private final boolean logResults;
    private static final Logger log = Logger.logger(DeepSolverTimer.class);

    public DeepSolverTimer(int nEmpty) {
        this(nEmpty, false);
    }

    public DeepSolverTimer(int nEmpty, boolean logResults) {
        this.logResults = logResults;
        pvs = SampleGames.vongPositions(nEmpty);
        log.info(pvs.size() + " distinct pvs at " + nEmpty + " empty");
    }

    public static void main(String[] args) {
        // warm up Hot Spot
        new DeepSolverTimer(18).run();

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

    private Solver solver;

    @Override public long nNodes() {
        return solver.getNodeStats();
    }

    @Override public void run() {
        this.solver = new Solver();

        for (PositionValue pv : pvs) {
            final int score = solver.solve(pv.mover, pv.enemy);
            if (logResults) {
                log.info("score : " + score);
            }
        }
    }

    public void runMultithreaded() {
        final ExecutorService executorService = Executors.newFixedThreadPool(8);
        final ArrayList<Future<Long>> futures = new ArrayList<>();

        final long t0 = System.currentTimeMillis();

        for (int i = 0; i < pvs.size(); i++) {
            final SolveTask task = new SolveTask(pvs.get(i), i);
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

        final long dt = System.currentTimeMillis() - t0;

        final long kn = nNodes / 1000;
        log.info(String.format("Total node count at %d: %,d kn / %,d ms = %3.1f Mn/s", pvs.get(0).nEmpty(), kn, dt, kn/(double)dt));
    }

    private class SolveTask implements Callable<Long> {
        private final PositionValue pv;
        private final int i;

        public SolveTask(PositionValue pv, int i) {
            this.pv = pv;
            this.i = i;
        }

        @Override public Long call() throws Exception {
            final Solver solver = new Solver();
            final int score = solver.solve(pv.mover, pv.enemy);
            final long nNodes = solver.getNodeStats();
            if (logResults) {
                final String msg = String.format("position %2d:   score %+3d   %,6d kn", i, score, nNodes / 1000);
                log.info(msg);
            }
            return nNodes;
        }
    }
}
