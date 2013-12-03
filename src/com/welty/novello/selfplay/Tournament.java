package com.welty.novello.selfplay;

import com.orbanova.common.misc.Vec;
import com.orbanova.common.misc.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 */
public class Tournament implements Runnable {
    public static void main(String[] args) throws Exception {
        if (args.length ==0) {
            System.err.println("usage: player1,player2,...");
        }
        final Player[] players = Players.players(args[0]);
        new Tournament(players).run();
    }


    private final Player[] players;

    private Tournament(Player[] players) {
        this.players = players;
    }

    public void run() {
        System.out.println("Starting tournament...");
        final long t0 = System.currentTimeMillis();
        double[] scores = new double[players.length];

        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        final List<Future<Result>> futures = new ArrayList<>();

        for (int i = 1; i < players.length; i++) {
            for (int j = 0; j < i; j++) {
                futures.add(executorService.submit(new SelfPlayTask(i, j)));
            }
        }
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
        Vec.timesEquals(scores, 1. / players.length);

        System.out.println();
        System.out.println("Tournament results:");
        final View sorter = View.getSortedView(scores).reverse();
        sorter.reorder(players);
        scores = sorter.reorderOf(scores);
        for (int i = 0; i < players.length; i++) {
            System.out.format("%+5.1f  %s%n", scores[i], players[i]);
        }
        System.out.println();
        final long dt = System.currentTimeMillis() - t0;
        System.out.format("Tournament complete in %5.1f s", dt*.001);
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

        public SelfPlayTask(int i, int j) {
            this.i = i;
            this.j = j;
        }

        @Override public Result call() throws Exception {
            final Player black = players[i];
            final Player white = players[j];
            final double averageResult = new SelfPlaySet(black, white, 0, false).call().averageResult;
            System.out.format("%+5.1f  %s vs %s%n", averageResult, black, white);
            return new Result(i, j, averageResult);
        }
    }

}
