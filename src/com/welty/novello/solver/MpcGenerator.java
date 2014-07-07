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

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.DefaultThreadLocal;
import com.welty.novello.core.MeValue;
import com.orbanova.common.gui.ProgressUpdater;
import com.welty.novello.eval.CoefficientEval;
import com.welty.novello.eval.EvalStrategies;
import com.welty.novello.eval.MvGenerator;
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

        final CoefficientEval eval = (CoefficientEval)Players.eval(evalName);
        final Path outputPath = eval.getCoeffDir().resolve("mpc.txt");

        final List<MeValue> pvs = getPvs(limit);
        final ExecutorService executorService = Executors.newFixedThreadPool(4);

        try (ProgressUpdater pu = new ProgressUpdater("Generating MPC", pvs.size())) {
            try (BufferedWriter out = Files.newBufferedWriter(outputPath, Charset.defaultCharset())) {
                for (MeValue pv : pvs) {
                    executorService.submit(new MpcPrinter(out, pv, maxDepth, pu));
                }
                executorService.shutdown();
                executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
            }
        }


        log.info("MPC generation complete");
    }

    private static class MpcPrinter implements Runnable {
        private static final DefaultThreadLocal<MidgameSearcher> searches = new DefaultThreadLocal<>(MidgameSearcher.class);
        private final BufferedWriter out;
        private final MeValue pv;
        private final int maxDepth;
        private final ProgressUpdater progressUpdater;

        MpcPrinter(BufferedWriter out, MeValue pv, int maxDepth, ProgressUpdater progressUpdater) {
            this.out = out;
            this.pv = pv;
            this.maxDepth = maxDepth;
            this.progressUpdater = progressUpdater;
        }

        @Override public void run() {
            MidgameSearcher midgameSearcher = searches.getOrCreate();

            final StringBuilder sb = new StringBuilder();
            sb.append(String.format("%2d ", pv.nEmpty()));
            for (int depth = 0; depth <= maxDepth; depth++) {
                final int score = midgameSearcher.calcScore(pv.mover, pv.enemy, depth);
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
    private static List<MeValue> getPvs(int limit) throws IOException {
        final List<MeValue> pvs = new MvGenerator(EvalStrategies.strategy("e")).getMvs();
        final ArrayList<MeValue> result = new ArrayList<>();
        final int[] counts = new int[64];
        for (int i = 0; i < pvs.size(); i++) {
            if (i % 100 == 0) {
                final MeValue pv = pvs.get(i);
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
