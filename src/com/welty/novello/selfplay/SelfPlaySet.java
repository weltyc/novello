package com.welty.novello.selfplay;

import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Props;
import com.welty.novello.eval.CoefficientEval;
import com.welty.novello.core.PositionValue;
import com.welty.novello.core.Position;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 */
public class SelfPlaySet {
    public static void main(String[] args) throws IOException {
        if (args.length!=2) {
            System.err.println("usage: blackPlayerName whitePlayerName");
            System.err.println(" for example a1:2 ntest:2");
        }
        final Player black = Players.player(args[0]);
        final Player white = Players.player(args[1]);

        final Result result = new SelfPlaySet(black, white, 2, true).call();
        System.out.format("%s vs %s: average result = %.1f\n", black, white, result.averageResult);
        System.out.format("%,d position evaluations performed.\n", CoefficientEval.nEvals());
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

        final HashSet<Position> alreadySeen = new HashSet<>();

        Position startPosition;
        int nComplete = 0;

        double sum = 0;
        double sumSq = 0;

        String hostName = Props.getHostName();

        while (null != (startPosition = generator.next())) {
            // only play positions where we have not seen a reflection previously
            // this means we won't get 8 of each game.
            if (alreadySeen.add(startPosition.minimalReflection())) {
                if (startPosition.hasLegalMove()) {
                    final int netResult;
                    final boolean printDetails = nComplete < nToPrint;
                    final int flags = printDetails ? -1 : 0;
                    final MutableGame result = new SelfPlayGame(startPosition, black, white, hostName, flags, 0).call();
                    pvs.addAll(result.calcPositionValues());
                    if (white != black) {
                        final MutableGame result2 = new SelfPlayGame(startPosition, white, black, hostName, flags, 0).call();
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
