package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;

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
    public static void main(String[] args) throws IOException {
        final Path path = Paths.get("ffo");

        double totalSeconds = 0;
        double totalMn = 0;

        try (final DirectoryStream<Path> files = Files.newDirectoryStream(path)) {
            for (Path file : files) {
                final Solver solver = new Solver();
                final List<String> strings = Files.readAllLines(file, Charset.defaultCharset());
                final Position position = new Position(strings.get(0), strings.get(1).toLowerCase().startsWith("black"));
                final long t0 = System.currentTimeMillis();
                final MoveScore moveScore = solver.solveWithMove(position.mover(), position.enemy());
                final double seconds = 0.001 * (System.currentTimeMillis() - t0);
                totalSeconds += seconds;

                final double mn = solver.nodeCounts.getNNodes() *1e-6;
                totalMn += mn;
                System.out.format("%s  %s %3d  %6.1fs  %d empty  %7.1f Mn    %4.1f Mn/s\n", strings.get(2).substring(18),
                        BitBoardUtils.sqToText(moveScore.sq), moveScore.score, seconds, position.nEmpty(), mn, mn/seconds);
            }
        }
        System.out.format("Total:       %6.1fs            %7.1f Gn    %4.1f Mn/s\n", totalSeconds, totalMn*0.001, totalMn/totalSeconds);
    }
}
