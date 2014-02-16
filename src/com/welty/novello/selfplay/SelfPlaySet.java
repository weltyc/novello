package com.welty.novello.selfplay;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.MeValue;
import com.welty.novello.core.MutableGame;
import com.welty.novello.core.NovelloUtils;
import com.welty.novello.core.Position;
import com.welty.othello.core.Engineering;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

/**
 */
public class SelfPlaySet {
    Logger log = Logger.logger(SelfPlaySet.class);

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("usage: blackPlayerName whitePlayerName [# games]");
            System.err.println(" for example a1:2 ntest:2");
        }
        final SyncPlayer black = Players.player(args[0]);
        final SyncPlayer white = Players.player(args[1]);

        final MatchResultListener[] listeners = {new MatchPrinter(2), new StatPrinter()};
        final double result = run(black, white, listeners);
        System.out.format("%s vs %s: average result = %.1f\n", black, white, result);
    }

    /**
     * Create a SelfPlaySet and run it
     * <p/>
     * While this may run multithreaded, all listeners will receive results in the calling thread
     * and therefore need not be thread safe.
     * <p/>
     * If syncEngine1==syncEngine2, this will play only one game per match. Otherwise each match will contain
     * two games from the same start position, the first with syncEngine1 starting the second with syncEngine2 starting.
     *
     * @param syncEngine1 first player
     * @param syncEngine2 second player
     * @param listeners   listeners to match result
     * @return average match result (player 1 disks - player 2 disks).
     */
    public static double run(SyncPlayer syncEngine1, SyncPlayer syncEngine2, MatchResultListener... listeners) {
        return new SelfPlaySet(syncEngine1, syncEngine2, listeners).call();
    }

    private final @NotNull SyncPlayer syncEngine1;
    private final @NotNull SyncPlayer syncEngine2;
    private final @NotNull MatchResultListener[] matchResultListeners;

    /**
     * Construct a SelfPlaySet.
     * <p/>
     * call() plays the games.
     *
     * @param syncEngine1          first player
     * @param syncEngine2          second player
     * @param matchResultListeners listeners to results of each match
     */
    private SelfPlaySet(@NotNull SyncPlayer syncEngine1, @NotNull SyncPlayer syncEngine2, @NotNull MatchResultListener... matchResultListeners) {
        this.syncEngine1 = syncEngine1;
        this.syncEngine2 = syncEngine2;
        this.matchResultListeners = matchResultListeners;
    }

    /**
     * Play all matches.
     * <p/>
     * If syncEngine1==syncEngine2, plays one game from each start position. Otherwise plays two.
     *
     * @return average match score (syncEngine1 disks - syncEngine2 disks).
     */
    private double call() {
        log.info("Starting " + syncEngine1 + " vs " + syncEngine2);

        final String hostName = NovelloUtils.getHostName();
        final List<Position> startPositions = generateStartPositions();

        int nComplete = 0;
        double sum = 0;
        for (Position startPosition : startPositions) {
            final int netResult;
            final MutableGame result = new SelfPlayGame(startPosition, syncEngine1, syncEngine2, hostName, 0).call();
            final MutableGame result2;
            if (syncEngine2 != syncEngine1) {
                result2 = new SelfPlayGame(startPosition, syncEngine2, syncEngine1, hostName, 0).call();
                netResult = (result.netScore() - result2.netScore());
            } else {
                // if the same player plays both sides we don't need to play the return games
                netResult = result.netScore();
                result2 = null;
            }
            sum += netResult;
            nComplete++;
            for (MatchResultListener listener : matchResultListeners) {
                listener.handle(nComplete, netResult, result, result2);
            }

        }
        for (MatchResultListener listener : matchResultListeners) {
            listener.onMatchesComplete(nComplete);
        }
        return sum / nComplete;
    }

    private static List<Position> generateStartPositions() {
        final HashSet<Position> alreadySeen = new HashSet<>();
        final StartPosGenerator generator = new StartPosGenerator(9);
        final List<Position> startPositions = new ArrayList<>();

        Position startPosition;
        while (null != (startPosition = generator.next())) {
            // only play positions where we have not seen a reflection previously
            // this means we won't get 8 of each game.
            if (alreadySeen.add(startPosition.minimalReflection()) && startPosition.hasLegalMove()) {
                startPositions.add(startPosition);
            }

        }

        Collections.shuffle(startPositions, new Random(1337));

        return startPositions;
    }

    public interface MatchResultListener {
        /**
         * Handle the results of a match
         * <p/>
         * The match may have one game (if syncEngine1==syncEngine2) or two games (otherwise). If there are two games, both
         * will be from the same starting position; the first with syncEngine1 starting, the second with syncEngine2 starting.
         *
         * @param nComplete # of matches completed so far
         * @param netResult syncEngine1 disks - player 2 disks
         * @param game1     first game of the completed match.
         * @param game2     second game of the match, or null if there was no second game.
         */
        void handle(int nComplete, double netResult, @NotNull MutableGame game1, @Nullable MutableGame game2);

        /**
         * Notify the listener that the set of matches is complete.
         *
         * @param nComplete # of matches completed
         */
        void onMatchesComplete(int nComplete);
    }

    public static class ProgressBarUpdater implements MatchResultListener {
        private final JProgressBar progressBar;

        public ProgressBarUpdater(JProgressBar progressBar) {
            this.progressBar = progressBar;
            progressBar.setMaximum(50777);
        }

        public void handle(int nComplete, double netResult, @NotNull MutableGame game1, @Nullable MutableGame game2) {
            progressBar.setValue(nComplete);
        }

        @Override public void onMatchesComplete(int nComplete) {
            progressBar.setValue(nComplete);
            progressBar.setMaximum(nComplete);
        }
    }

    public static class StatPrinter implements MatchResultListener {
        private static final Logger log = Logger.logger(StatPrinter.class);

        double sumSq = 0;
        double sum = 0;
        private final long interval;
        double t1;
        double t2;
        long nextPrint;

        public StatPrinter() {
            this(5000);
        }

        public StatPrinter(int interval) {
            this.interval = interval;
            nextPrint = System.currentTimeMillis() + interval;
        }

        @Override public void handle(int nComplete, double netResult, @NotNull MutableGame game1, @Nullable MutableGame game2) {
            sum += netResult;
            sumSq += netResult * netResult;
            t1 += game1.time(true);
            t2 += game1.time(false);
            if (game2 != null) {
                t1 += game2.time(false);
                t2 += game2.time(true);
            }
            final long now = System.currentTimeMillis();
            if (now >= nextPrint) {
                printStats(nComplete);
                nextPrint = now + interval;
            }
        }

        @Override public void onMatchesComplete(int nComplete) {
            printStats(nComplete);
        }

        private void printStats(int nComplete) {
            final double variance = sumSq - sum * sum / nComplete;
            final double stdErr = Math.sqrt(variance);
            final double tStat = sum / stdErr;
            log.info(String.format("%,6d matches: average result = %+.3g +/-%3.2g. T ~ %5.1f.  %ss vs%ss."
                    , nComplete, sum / nComplete, stdErr / nComplete, tStat
                    , Engineering.compactFormat(t1 / nComplete), Engineering.compactFormat(t2 / nComplete))
            );
        }
    }

    public static class MatchPrinter implements MatchResultListener {
        private final int nToPrint;

        public MatchPrinter(int nToPrint) {
            this.nToPrint = nToPrint;
        }

        @Override public void handle(int nComplete, double netResult, @NotNull MutableGame game1, @Nullable MutableGame game2) {
            if (nComplete <= nToPrint) {
                System.out.println(game1.toGgf());
                if (game2 != null) {
                    System.out.println(game2.toGgf());
                }
            }
        }

        @Override public void onMatchesComplete(int nComplete) {
            // do nothing
        }
    }

    public static class PvCollector implements MatchResultListener {
        public final List<MeValue> pvs = new ArrayList<>();

        @Override public void handle(int nComplete, double netResult, @NotNull MutableGame game1, @Nullable MutableGame game2) {
            pvs.addAll(game1.calcPositionValues());
            if (game2 != null) {
                pvs.addAll(game2.calcPositionValues());
            }
        }

        @Override public void onMatchesComplete(int nComplete) {
            // do nothing
        }
    }
}
