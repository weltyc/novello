package com.welty.novello.solver;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.PositionValue;
import com.welty.novello.core.ProgressUpdater;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.CoefficientEval;
import com.welty.novello.selfplay.Players;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MpcGenerator {
    private static final Logger log = Logger.logger(MpcGenerator.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 3) {
            System.err.println("usage: {evalName} {maxDepth} {limit}");
            System.exit(-1);
        }

        final String evalName = args[0];
        final int maxDepth = Integer.parseInt(args[1]);
        final int limit = Integer.parseInt(args[2]);

        final CoefficientEval eval = Players.eval(evalName);
        final Path outputPath = eval.getCoeffDir().resolve("mpc.txt");

        final List<PositionValue> pvs = getPvs(limit);
        final ExecutorService executorService = Executors.newFixedThreadPool(4);

        try (ProgressUpdater pu = new ProgressUpdater("Generating MPC", pvs.size())) {
            try (BufferedWriter out = Files.newBufferedWriter(outputPath, Charset.defaultCharset())) {
                for (PositionValue pv : pvs) {
                    executorService.submit(new MpcPrinter(out, eval, pv, maxDepth, pu));
                }
                executorService.shutdown();
                executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
            }
        }


        log.info("MPC generation complete");
    }

    private static class MpcPrinter implements Runnable {
        private static final ThreadLocal<Search> searches = new ThreadLocal<>();
        private final BufferedWriter out;
        private final CoefficientEval eval;
        private final PositionValue pv;
        private final int maxDepth;
        private final ProgressUpdater progressUpdater;

        MpcPrinter(BufferedWriter out, CoefficientEval eval, PositionValue pv, int maxDepth, ProgressUpdater progressUpdater) {
            this.out = out;
            this.eval = eval;
            this.pv = pv;
            this.maxDepth = maxDepth;
            this.progressUpdater = progressUpdater;
        }

        @Override public void run() {
            Search search = searches.get();
            if (search == null) {
                search = new Search(new Counter(eval), 0);
                searches.set(search);
            }

            final StringBuilder sb = new StringBuilder();
            sb.append(String.format("%2d ", pv.nEmpty()));
            for (int depth = 0; depth <= maxDepth; depth++) {
                final int score = search.calcScore(pv.mover, pv.enemy, depth, false);
                sb.append(String.format("%+5d ", score));
            }
            sb.append('\n');

            synchronized(out) {
                try {
                    out.write(sb.toString());
                } catch (IOException e) {
                    System.err.println(e);
                }
            }
            progressUpdater.update();
        }
    }


    /**
     * Select positions for MPC calculations
     *
     * @param limit maximum number of positions per empty
     * @return positionValues, with 1/100 selected, up to a maximum of limit per empty
     * @throws IOException
     */
    private static List<PositionValue> getPvs(int limit) throws IOException {
        final List<PositionValue> pvs = CoefficientCalculator.loadOrCreatePvs("b1-2");
        final ArrayList<PositionValue> result = new ArrayList<>();
        final int[] counts = new int[64];
        for (int i = 0; i < pvs.size(); i++) {
            if (i % 100 == 0) {
                final PositionValue pv = pvs.get(i);
                final int nEmpty = pv.nEmpty();
                if (counts[nEmpty] < limit) {
                    result.add(pv);
                    counts[nEmpty]++;
                }
            }
        }
        log.info(result.size() + " of " + pvs.size() + " positions selected.");
        StringBuilder notFull = new StringBuilder();
        notFull.append("not full: ");
        for (int i = 0; i < 64; i++) {
            if (counts[i] < limit) {
                notFull.append(String.format("%d(%d),", i, counts[i]));
            }
        }
        log.info(notFull.toString());
        return result;
    }
}
