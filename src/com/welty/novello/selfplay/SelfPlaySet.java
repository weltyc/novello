package com.welty.novello.selfplay;

import com.welty.novello.eval.PositionValue;
import com.welty.novello.solver.BitBoard;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.welty.novello.eval.EvalStrategies.eval4;

/**
 */
public class SelfPlaySet {
    public static void main(String[] args) {
//        final Player black = new EvalPlayer(EvalStrategies.current);
        final Player black = new Bobby();
        final Player white = new EvalPlayer(eval4, "A");
        new SelfPlaySet(black, white, 2).call();
    }

    private final @NotNull Player black;
    private final @NotNull Player white;
    private final int nToPrint;

    /**
     * Construct a SelfPlaySet.
     *
     * call() plays the games.
     *
     * @param black black player
     * @param white white player
     * @param nToPrint print the first nToPrint games to System.out.
     */
    public SelfPlaySet(@NotNull Player black, @NotNull Player white, int nToPrint) {
        this.black = black;
        this.white = white;
        this.nToPrint = nToPrint;
    }

    public List<PositionValue> call() {
        final List<PositionValue> pvs = new ArrayList<>();

        final StartPosGenerator generator = new StartPosGenerator(9);

        final HashSet<BitBoard> alreadySeen = new HashSet<>();

        BitBoard startPosition;
        int nComplete = 0;

        double sum = 0;
        double sumSq = 0;

        while (null != (startPosition = generator.next())) {
            // only play positions where we have not seen a reflection previously
            // this means we won't get 8 of each game.
            if (alreadySeen.add(startPosition.minimalReflection())) {
                if (startPosition.hasLegalMove()) {
                    final int netResult;
                    final SelfPlayGame.Result result = new SelfPlayGame(startPosition, black, white, nComplete < nToPrint).call();
                    pvs.addAll(result.getPositionValues());
                    if (white != black) {
                        final SelfPlayGame.Result result2 = new SelfPlayGame(startPosition, white, black, false).call();
                        pvs.addAll(result2.getPositionValues());
                        netResult = (result.netScore - result2.netScore);
                    } else {
                        // if the same player plays both sides we don't need to play the return games
                        netResult = result.netScore;
                    }
                    sum += netResult;
                    sumSq += netResult * netResult;
                    nComplete++;
                    if (nComplete % 5000 == 0) {
                        printStats(nComplete, sum, sumSq);
                    }
                }
            }

        }

        printStats(nComplete, sum, sumSq);
        return pvs;
    }

    private static void printStats(int nComplete, double sum, double sumSq) {
        final double variance = sumSq - sum * sum / nComplete;
        final double stdErr = Math.sqrt(variance);
        final double tStat = sum / stdErr;
        System.out.format("after %,6d matches, average result = %.3g +/- %.2g. T ~ %5.3g%n"
                , nComplete, sum / nComplete, stdErr / nComplete, tStat);
    }
}
