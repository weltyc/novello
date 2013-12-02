package com.welty.novello.selfplay;

import com.welty.novello.eval.Eval;
import com.welty.novello.eval.PositionValue;
import com.welty.novello.ntest.NTest;
import com.welty.novello.solver.BitBoard;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 */
public class SelfPlaySet {
    public static void main(String[] args) throws IOException {
        final Player black = Players.player("9A:2");
//        final Player white = Players.player("8A:2");
        final Player white = new NTest(1, false);
        final Result result = new SelfPlaySet(black, white, 2, true).call();
        System.out.format("%s vs %s: average result = %.1f\n", black, white, result.averageResult);
        System.out.format("%,d position evaluations performed.\n", Eval.nEvals());
    }

    private final @NotNull Player black;
    private final @NotNull Player white;
    private final int nToPrint;
    private final boolean printUpdates;

    /**
     * Construct a SelfPlaySet.
     * <p/>
     * call() plays the games.
     *
     * @param black        black player
     * @param white        white player
     * @param nToPrint     print the first nToPrint games to System.out.
     * @param printUpdates if true, print out statistics during the course of the set
     */
    public SelfPlaySet(@NotNull Player black, @NotNull Player white, int nToPrint, boolean printUpdates) {
        this.black = black;
        this.white = white;
        this.nToPrint = nToPrint;
        this.printUpdates = printUpdates;
    }

    public Result call() {
        final List<PositionValue> pvs = new ArrayList<>();

        final StartPosGenerator generator = new StartPosGenerator(9);

        final HashSet<BitBoard> alreadySeen = new HashSet<>();

        BitBoard startPosition;
        int nComplete = 0;

        double sum = 0;
        double sumSq = 0;

        String hostName = getHostName();

        while (null != (startPosition = generator.next())) {
            // only play positions where we have not seen a reflection previously
            // this means we won't get 8 of each game.
            if (alreadySeen.add(startPosition.minimalReflection())) {
                if (startPosition.hasLegalMove()) {
                    final int netResult;
                    final boolean printDetails = nComplete < nToPrint;
                    final int searchFlags = printDetails ? -1 : 0;
                    final MutableGame result = new SelfPlayGame(startPosition, black, white, hostName, printDetails, searchFlags).call();
                    pvs.addAll(result.calcPositionValues());
                    if (white != black) {
                        final MutableGame result2 = new SelfPlayGame(startPosition, white, black, hostName, printDetails, searchFlags).call();
                        pvs.addAll(result2.calcPositionValues());
                        netResult = (result.netScore() - result2.netScore());
                    } else {
                        // if the same player plays both sides we don't need to play the return games
                        netResult = result.netScore();
                    }
                    sum += netResult;
                    sumSq += netResult * netResult;
                    nComplete++;
                    if (printUpdates && nComplete % 5000 == 0) {
                        printStats(nComplete, sum, sumSq);
                    }
                }
            }

        }

        if (printUpdates) {
            printStats(nComplete, sum, sumSq);
        }
        return new Result(pvs, sum / nComplete);
    }

    private static String getHostName() {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "Unknown";
        }
        return hostName;
    }

    private static void printStats(int nComplete, double sum, double sumSq) {
        final double variance = sumSq - sum * sum / nComplete;
        final double stdErr = Math.sqrt(variance);
        final double tStat = sum / stdErr;
        System.out.format("after %,6d matches, average result = %.3g +/- %.2g. T ~ %5.3g%n"
                , nComplete, sum / nComplete, stdErr / nComplete, tStat);
    }

    public static class Result {
        public final List<PositionValue> pvs;
        final double averageResult;

        Result(List<PositionValue> pvs, double averageResult) {
            this.pvs = pvs;
            this.averageResult = averageResult;
        }
    }
}
