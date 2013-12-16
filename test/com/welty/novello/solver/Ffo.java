package com.welty.novello.solver;

import com.welty.novello.core.*;
import com.welty.novello.selfplay.Players;

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
     *
     * @param args see flags, above
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final Path path = Paths.get("ffo");
        final boolean useMidgame = NovelloUtils.getShortOptions(args).contains("m");

        System.out.println("Starting ffo test using " + (useMidgame ? "midgame" : "endgame") + " search");
        double totalSeconds = 0;
        double totalMn = 0;
        int nCorrectMoves = 0;

        try (final DirectoryStream<Path> files = Files.newDirectoryStream(path)) {
            for (Path file : files) {
                final List<String> strings = Files.readAllLines(file, Charset.defaultCharset());
                final Position position = new Position(strings.get(0), strings.get(1).toLowerCase().startsWith("black"));

                final Object searcher;

                if (useMidgame) {
                    searcher = new Search(new Counter(Players.currentEval()), 0);
                } else {
                    searcher = new Solver();
                }

                final long t0 = System.currentTimeMillis();

                final MoveScore moveScore;
                final Counts counts;
                final int score;

                if (useMidgame) {
                    final Search search = (Search) searcher;
                    moveScore = search.calcMove(position, position.calcMoves(), position.nEmpty(), true);
                    counts = search.counts();
                    score = moveScore.score/100;

                } else {
                    final Solver solver = (Solver) searcher;
                    moveScore = solver.solveWithMove(position.mover(), position.enemy());
                    counts = solver.getCounts();
                    score = moveScore.score;
                }

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
                System.out.format("%s  %s %3d  %6.1fs  %d empty  %7.1f Mn    %4.1f Mn/s  %s\n", strings.get(2).substring(18),
                        BitBoardUtils.sqToText(moveScore.sq), score, seconds, position.nEmpty(), mn, mn / seconds
                        , !hasCorrectMove ? "WRONG MOVE" : "");
            }
        }
        System.out.format("Total:       %6.1fs            %7.1f Gn    %4.1f Mn/s   %d/20 correct moves\n", totalSeconds
                , totalMn * 0.001, totalMn / totalSeconds, nCorrectMoves);
    }
}
