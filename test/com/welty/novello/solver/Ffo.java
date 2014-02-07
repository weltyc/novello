package com.welty.novello.solver;

import com.welty.novello.core.*;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.ntest.NBoardSyncEngine;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SyncPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 */
public class Ffo {
    private static String[] answers = "A2;H4;G2;C7,G3;B8,D2;B2;B3;G2;F6;E1;D8;A3,E2;A3;D8;C7;G6,G4,B7,E2;H5;A6;G1;H4,E8,G8".split(";");

    /**
     * Flags:
     * <p/>
     * -m use midgame search
     * -n use NTest
     *
     * @param args see flags, above
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final Path path = Paths.get("ffo");
        final String options = NovelloUtils.getShortOptions(args);

        double totalSeconds = 0;
        double totalMn = 0;
        int nCorrectMoves = 0;

        final Searcher searcher;

        if (options.contains("m")) {
            searcher = new Midgame(new MidgameSearcher(new Counter(Players.currentEval())));
        } else if (options.contains("n")) {
            searcher = new NtestSearcher();
        } else {
            searcher = new Endgame(new Solver());
        }
        System.out.println("Starting ffo test using " + searcher);


        try (final DirectoryStream<Path> files = Files.newDirectoryStream(path)) {
            for (Path file : files) {
                final List<String> strings = Files.readAllLines(file, Charset.defaultCharset());
                final Position position = new Position(strings.get(0), strings.get(1).toLowerCase().startsWith("black"));

                final long t0 = System.currentTimeMillis();

                final Counts c0 = searcher.getCounts();

                final MoveScore moveScore = searcher.getMoveScore(position);
                final int score = moveScore.centidisks / CoefficientCalculator.DISK_VALUE;

                final Counts counts = searcher.getCounts().minus(c0);

                final double seconds = 0.001 * (System.currentTimeMillis() - t0);
                totalSeconds += seconds;

//                final double mn = solver.getCounts().nFlips *1e-6;
                final double mn = counts.nFlips * 1e-6;

                totalMn += mn;
                final int ffoId = Integer.parseInt(strings.get(2).substring(19));
                final String bestMoves = answers[ffoId - 40];
                final boolean hasCorrectMove = bestMoves.contains(BitBoardUtils.sqToText(moveScore.sq));
                if (hasCorrectMove) {
                    nCorrectMoves++;
                }
                System.out.format("%s  %s %+6.2f  %6.1fs  %d empty  %7.1f Mn    %4.1f Mn/s  %s\n", strings.get(2).substring(18),
                        BitBoardUtils.sqToText(moveScore.sq), score * 0.01, seconds, position.nEmpty(), mn, mn / seconds
                        , !hasCorrectMove ? "WRONG MOVE" : "");
            }
        }
        System.out.format("Total:       %6.1fs            %7.1f Gn    %4.1f Mn/s   %d/20 correct moves\n", totalSeconds
                , totalMn * 0.001, totalMn / totalSeconds, nCorrectMoves);
    }

    public static interface Searcher {
        /**
         * Get the number of nodes this Searcher has visited since it was created
         *
         * @return number of nodes since this Searcher was created
         */
        @NotNull Counts getCounts();

        /**
         * Get the best move, and the score in disks
         *
         * @param position position to evaluate
         * @return best move and score
         */
        @NotNull MoveScore getMoveScore(Position position);
    }

    public static class Midgame implements Searcher {
        private final MidgameSearcher searcher;

        public Midgame(MidgameSearcher searcher) {
            this.searcher = searcher;
        }

        @NotNull @Override public Counts getCounts() {
            return searcher.getCounts();
        }

        @NotNull @Override public MoveScore getMoveScore(Position position) {
            return searcher.getMoveScore(position, position.calcMoves(), position.nEmpty());
        }

        @Override public String toString() {
            return "midgame";
        }
    }

    public static class Endgame implements Searcher {
        private final Solver solver;

        public Endgame(Solver solver) {
            this.solver = solver;
        }

        @NotNull @Override public Counts getCounts() {
            return solver.getCounts();
        }

        @NotNull @Override public MoveScore getMoveScore(Position position) {
            final MoveScore moveScore = solver.getMoveScore(position.mover(), position.enemy());
            return new MoveScore(moveScore.sq, moveScore.centidisks * CoefficientCalculator.DISK_VALUE);
        }

        @Override public String toString() {
            return "Solver";
        }
    }

    public static class NtestSearcher implements Searcher {
        private final SyncPlayer player = new SyncPlayer(new NBoardSyncEngine("ntest", 50, false), 50);

        @NotNull @Override public Counts getCounts() {
            return new Counts(0, 0);
        }

        @NotNull @Override public MoveScore getMoveScore(Position position) {
            return player.calcMove(position);
        }

        @Override public String toString() {
            return "ntest";
        }
    }
}
