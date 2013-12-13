package com.welty.novello.solver;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.Counts;
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
        warmUpHotSpot();


//        System.out.println(Typical.timing(new DeepSolverTimer(20)));

        new DeepSolverTimer(20, true).cost();
//        final long t0 = System.currentTimeMillis();
//        final DeepSolverTimer timer = new DeepSolverTimer(24, true);
//        timer.run();
//        final long dt = System.currentTimeMillis() - t0;
//        final long getCounts = timer.getCounts();
//        System.out.format("%,d ms elapsed; %,d total nodes\n", dt, getCounts);
//        timer.solver.dumpStatistics();
//        System.out.println(timer.solver.nodeCounts.getNodeCountsByDepth());
//        System.out.println(timer.solver.hashTables.stats());
    }

    /**
     * Allow the JVM to efficiently compile classes
     */
    static void warmUpHotSpot() {
        new DeepSolverTimer(18).cost();
    }

    /**
     * Solve the positions with 8 threads.
     *
     * @return total node counts for solution
     */
    @Override public double cost() {
        final ExecutorService executorService = Executors.newFixedThreadPool(8);
        final ArrayList<Future<Counts>> futures = new ArrayList<>();

        final long t0 = System.currentTimeMillis();

        for (int i = 0; i < pvs.size(); i++) {
            final SolveTask task = new SolveTask(pvs.get(i), i);
            futures.add(executorService.submit(task));
        }
        executorService.shutdown();
        Counts counts = new Counts(0,0);

        for (Future<Counts> future : futures) {
            try {
               counts = counts.plus(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        final double dt = (System.currentTimeMillis() - t0)/1000.;

        final long mf = counts.nFlips / 1000000;
        log.info(String.format("Total flip count at %d: %s / %,.0f s = %3.1f Mn/s", pvs.get(0).nEmpty(), counts, dt, mf/dt));

        return (double)counts.cost();
    }

    private class SolveTask implements Callable<Counts> {
        private final PositionValue pv;
        private final int i;

        public SolveTask(PositionValue pv, int i) {
            this.pv = pv;
            this.i = i;
        }

        @Override public Counts call() throws Exception {
            final Solver solver = new Solver();
            final int score = solver.solve(pv.mover, pv.enemy);
            final Counts counts = solver.getCounts();
            if (logResults) {
                final String msg = String.format("position %2d:   score %+3d %s", i, score, counts.toString(2));
                log.info(msg);
            }
            return counts;
        }
    }
}
