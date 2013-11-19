package com.welty.novello.selfplay;

import com.welty.novello.eval.Eval1;
import com.welty.novello.eval.PositionValue;
import com.welty.novello.solver.BitBoard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 */
public class SelfPlaySet {
    public static void main(String[] args) {
        call();
    }

    public static List<PositionValue> call() {
        final List<PositionValue> pvs = new ArrayList<>();

        final StartPosGenerator generator = new StartPosGenerator(9);
        final Player white = new EvalPlayer(new Eval1());
        final Player black = new Bobby();

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
                    final SelfPlayGame.Result result = new SelfPlayGame(startPosition, black, white, nComplete < 2).call();
                    final SelfPlayGame.Result result2 = new SelfPlayGame(startPosition, white, black, false).call();
                    final int netResult = (result.netScore - result2.netScore);
                    sum += netResult;
                    sumSq += netResult * netResult;
                    nComplete++;
                    if (nComplete % 1000 == 0) {
                        printStats(nComplete, sum, sumSq);
                    }
                    pvs.addAll(result.getPositionValues());
                    pvs.addAll(result2.getPositionValues());
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
                , nComplete, sum/nComplete, stdErr/nComplete, tStat);
    }
}
