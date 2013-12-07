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

        try (final DirectoryStream<Path> files = Files.newDirectoryStream(path)) {
            for (Path file : files) {
                final Solver solver = new Solver();
                final List<String> strings = Files.readAllLines(file, Charset.defaultCharset());
                final Position position = new Position(strings.get(0), strings.get(1).toLowerCase().startsWith("black"));
//                System.out.println(position);
                final long t0 = System.currentTimeMillis();
                final MoveScore moveScore = solver.solveWithMove(position.mover(), position.enemy());
                final long dt = System.currentTimeMillis() - t0;

                final double mn = solver.nodeCounts.getNNodes()*1e-6;
                System.out.format("%s  %s %3d  %6.1fs  %d empty  %5.1f Mn\n", strings.get(2).substring(18),
                        BitBoardUtils.sqToText(moveScore.sq), moveScore.score, 0.001 * dt, position.nEmpty(), mn);
            }
        }

    }
}
