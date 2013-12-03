package com.welty.novello.solver;

/**
 */
public class SolverTimer {
    /**
     * Number of times the test is run to make one round.
     */
    private static final int nIters = 16;

    /**
     * Timing test of the solver
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        System.out.println(System.getProperty("sun.arch.data.model") + "-bit JVM");
        final Solver solver = new Solver();
        Typical typical = timeRound(solver, nIters);
        final long nNodes = solver.nodeCounts.getNNodes();
        final double Mnps = nNodes / typical.sum * 0.001;
        final double nsPerNode = 1000 / Mnps;
        System.out.format("Typical %s ms. %.3g Mn; %.3g Mn/s; %.3g ns/n%n", typical, 1e-6 * nNodes / nIters, Mnps, nsPerNode);
        System.out.println(solver.nodeCounts.getNodeCountsByDepth());
        System.out.println(solver.hashTable.stats());
    }

    /**
     * warm up JVM hot spot
     */
    static {
        for (int i = 0; i < 2; i++) {
            SolverTest.testSolveValues();
        }
    }

    /**
     * Execute the solver nIters times and return an array of times in ms.
     *
     * @param nIters number of times to solve.
     * @return times, in milliseconds. One for each solve.
     */
    static Typical timeRound(Solver solver, int nIters) {
        final double[] timings = new double[nIters];

        for (int i = 0; i < nIters; i++) {
            final long t0 = System.currentTimeMillis();
//            SolverTest.testSolveValues(solver);
            DeepSolverTimer.run(solver);
            final long dt = System.currentTimeMillis() - t0;
            timings[i] = dt;
        }
        return new Typical(timings);
    }
}
