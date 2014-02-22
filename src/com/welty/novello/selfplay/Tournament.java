package com.welty.novello.selfplay;

import com.orbanova.common.feed.Feeds;
import com.orbanova.common.jsb.Control;
import com.orbanova.common.jsb.JSwingBuilder;
import com.orbanova.common.jsb.JsbFrame;
import com.orbanova.common.misc.Vec;
import com.orbanova.common.misc.View;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Run a tournament.
 */
public class Tournament implements Runnable {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("usage: player1 player2 ...");
            System.exit(-1);
        }
        new Tournament(args).run();
    }


    private final String[] playerNames;

    /**
     * Construct a tournament.
     * <p/>
     * Since SyncEngine is not thread-safe, a new player must be constructed for each Set he's playing in.
     * The playerName is used to construct a new SyncEngine for each set.
     *
     * @param playerNames names of players, for instance "d1s:2".
     */
    private Tournament(String[] playerNames) {
        this.playerNames = playerNames;
    }

    public void run() {
        System.out.println("Starting tournament...");
        final long t0 = System.currentTimeMillis();
        double[] scores = new double[playerNames.length];

        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        final List<Future<Result>> futures = new ArrayList<>();
        final List<Control> controls = new ArrayList<>();

        for (int i = 1; i < playerNames.length; i++) {
            for (int j = 0; j < i; j++) {
                final SelfPlayTask task = new SelfPlayTask(i, j);
                controls.add(task.control());
                futures.add(executorService.submit(task));
            }
        }

        final JsbFrame progressFrame = JSwingBuilder.frame("Tournament progress", JFrame.EXIT_ON_CLOSE, true,
                JSwingBuilder.controlGrid(Feeds.of(controls)));

        executorService.shutdown();

        // turn total scores into averages
        for (Future<Result> future : futures) {
            final Result result;
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            final double averageResult = result.averageScore;
            scores[result.iBlack] += averageResult;
            scores[result.iWhite] -= averageResult;
        }
        /**
         * divide by number of players, rather than number of games, so that
         * expected result = score difference
         */
        Vec.timesEquals(scores, 1. / playerNames.length);

        System.out.println();
        System.out.println("Tournament results:");
        final View sorter = View.getSortedView(scores).reverse();
        sorter.reorder(playerNames);
        scores = sorter.reorderOf(scores);
        for (int i = 0; i < playerNames.length; i++) {
            System.out.format("%+5.1f  %s%n", scores[i], playerNames[i]);
        }
        System.out.println();
        final long dt = System.currentTimeMillis() - t0;
        System.out.format("Tournament complete in %5.1f s", dt * .001);

        progressFrame.dispose();
    }

    private static class Result {
        final int iBlack;
        final int iWhite;
        final double averageScore;

        private Result(int iBlack, int iWhite, double averageScore) {
            this.iBlack = iBlack;
            this.iWhite = iWhite;
            this.averageScore = averageScore;
        }
    }

    private class SelfPlayTask implements Callable<Result> {
        private final int i;
        private final int j;
        private final @NotNull JProgressBar progressBar;

        public SelfPlayTask(int i, int j) {
            this.i = i;
            this.j = j;
            this.progressBar = new JProgressBar();
        }

        @Override public Result call() throws Exception {
            final SyncPlayer black = Players.player(playerNames[i]);
            final SyncPlayer white = Players.player(playerNames[j]);
            final double averageResult = SelfPlaySet.run(black, white, new SelfPlaySet.ProgressBarUpdater(progressBar));
            System.out.format("%+5.1f  %s vs %s%n", averageResult, black, white);
            return new Result(i, j, averageResult);
        }

        public Control control() {
            return JSwingBuilder.control(playerNames[i] + " vs " + playerNames[j], progressBar);
        }
    }

}
