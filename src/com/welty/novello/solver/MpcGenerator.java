package com.welty.novello.solver;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.PositionValue;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.CoefficientEval;
import com.welty.novello.selfplay.Players;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MpcGenerator {
    private static final Logger log = Logger.logger(MpcGenerator.class);

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("usage: {evalName} {maxDepth} {limit}");
            System.exit(-1);
        }

        final String evalName = args[0];
        final int maxDepth = Integer.parseInt(args[1]);
        final int limit = Integer.parseInt(args[2]);


        final CoefficientEval eval = Players.eval(evalName);
        final Search search = new Search(new Counter(eval, false), 0);
        final Path outputPath = eval.getCoeffDir().resolve("mpc.txt");

        final List<PositionValue> pvs = getPvs(limit);

        final ProgressMonitor progressMonitor = new ProgressMonitor(null, "Computing MPC data", "", 0, pvs.size());

        try (BufferedWriter out = Files.newBufferedWriter(outputPath, Charset.defaultCharset())) {
            int nComplete = 0;
            for (PositionValue pv : pvs) {
                out.write(String.format("%2d ", pv.nEmpty()));
                for (int depth = 0; depth <= maxDepth; depth++) {
                    final int score = search.calcScore(pv.mover, pv.enemy, depth, false);
                    out.write(String.format("%+5d ", score));
                }
                out.newLine();
                progressMonitor.setProgress(++nComplete);
            }
        }
        log.info("MPC generation complete");
        progressMonitor.close();
        System.exit(0);
    }

    /**
     * Select positions for MPC calculations
     *
     * @param limit maximum number of positions per empty
     * @return positionValues, with 1/100 selected, up to a maximum of limit per empty
     * @throws IOException
     */
    private static List<PositionValue> getPvs(int limit) throws IOException {
        final List<PositionValue> pvs = CoefficientCalculator.loadOrCreatePvs();
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
