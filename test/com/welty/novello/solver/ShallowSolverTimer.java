package com.welty.novello.solver;

/**
 */
public class ShallowSolverTimer implements Tunable {

    /**
     * warm up JVM hot spot
     */
    static {
        for (int i = 0; i < 2; i++) {
            SolverTest.testSolveValues();
        }
    }

    private Solver solver;

    public ShallowSolverTimer() {
    }

    @Override public long nNodes() {
        return solver.nodeCounts.getNNodes();
    }

    @Override public void run() {
        solver = new Solver();
        SolverTest.testSolveValues(solver);
    }

    /**
     * Number of times the test is run to make one round.
     */

    /**
     * Timing test of the solver
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        System.out.println(System.getProperty("sun.arch.data.model") + "-bit JVM");
        final int nIters = 16;

        final ShallowSolverTimer timer = new ShallowSolverTimer();
        final Typical typical = Typical.timing(timer, nIters);
        final Solver solver = timer.solver;
        final long nNodes = solver.nodeCounts.getNNodes();
        final double Mnps = nNodes / typical.sum * 0.001;
        final double nsPerNode = 1000 / Mnps;
        System.out.format("Typical %s ms. %.3g Mn; %.3g Mn/s; %.3g ns/n%n", typical, 1e-6 * nNodes / nIters, Mnps, nsPerNode);
        System.out.println(solver.nodeCounts.getNodeCountsByDepth());
        System.out.println(solver.hashTables.stats());
    }
}
