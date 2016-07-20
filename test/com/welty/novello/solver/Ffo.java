/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.solver;

import com.welty.novello.core.*;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.ntest.NBoardSyncEngine;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SyncPlayer;
import com.welty.novello.external.api.AbortCheck;
import com.welty.novello.external.gui.ExternalEngineManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.prefs.BackingStoreException;

public class Ffo {
    /**
     * Run the FFO test.
     * <p>
     * This runs FFO test positions 40-59 and prints out timing and node count information.
     * It also prints a message if the engine does not return an optimal move.
     * <p>
     * Flags:
     * <dl>
     * <dt>-m</dt> <dd> use midgame search</dd>
     * <dt>-n</dt> <dd> use NTest as the engine instead of Novello.</dd>
     * <dt>-t</dt> <dd> run one copy of ntest for each core simultaneously</dd>
     * </dl>
     * <p>
     * If the -m flag is used, the engine is not required to return optimal moves for all positions.
     * <p>
     * If -n is specified, the location of the NTest engine executable is retrieved from the
     * ExternalEngineManager using the name "ntest". NBoard also uses the ExternalEngineManager, so
     * it can be used to specify the executable location.
     * <p>
     * The positions for the FFO test are taken from the directory "ffo/".
     *
     * @param args see flags, above
     */
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final Path path = Paths.get("problem/fforum-40-59.obf");
        final String options = NovelloUtils.getShortOptions(args);
        final int nThreads = options.contains("t") ? Runtime.getRuntime().availableProcessors() : 1;
        final List<Problem> problems = loadProblems(path);
        System.out.println("Starting ffo test using options=" + options + " and " + nThreads + " thread" + (nThreads > 1 ? "s" : ""));

        ExecutorService service = Executors.newFixedThreadPool(nThreads);
        for (int i=0; i<nThreads; i++) {
            Runnable runnable = new Runnable() {
                @Override public void run() {
                    solveProblems(options, problems);
                }
            };
            service.submit(runnable);
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.DAYS);
    }

    private static void solveProblems(String options, List<Problem> problems) {
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


        for (Problem problem : problems) {
            final SolveResult result = SolveProblem(searcher, problem);
            totalMn += result.megaNodes;
            totalSeconds += result.seconds;
            nCorrectMoves += result.nCorrectMoves;
        }

        System.out.format("Total:       %6.1fs            %7.1f Gn    %4.1f Mn/s   %d/%d correct moves\n", totalSeconds
                , totalMn * 0.001, totalMn / totalSeconds, nCorrectMoves, problems.size());
    }

    private static @NotNull List<Problem> loadProblems(Path path) throws IOException {
        final List<Problem> problems = new ArrayList<>();
        int problemNumber = calcFirstId(path);
        for (String line : Files.readAllLines(path)) {
            String[] parts = line.split("\\s*;\\s*", 2);
            final Board board = Board.of(parts[0]);
            String problemName=String.format("#%2d", problemNumber++);
            final Problem problem = new Problem(board, calcBestMoves(parts[1]), problemName);
            problems.add(problem);
        }

        return problems;
    }

    /**
     * Guess the id of the first problem in the file from the path.
     *
     * If can't guess, return 1.
     *
     * @param path path of the problem input file
     * @return id of the problem
     */
    private static int calcFirstId(Path path) {
        String fn = path.getFileName().toString();
        for (String part : fn.split("-")) {
            try {
                return Integer.parseInt(part);
            }
            catch(NumberFormatException e) {
                // this isn't a number, try the next one.
            }
        }

        return 1;
    }

    @NotNull private static String calcBestMoves(String bestMoveText) {
        final String[] moveResultTexts = bestMoveText.split("\\s*;\\s*");
        final int expected = new MoveResult(moveResultTexts[0]).value;
        StringBuilder bestMoveTexts = new StringBuilder();

        for (String moveResultText : moveResultTexts) {
            final MoveResult mr = new MoveResult(moveResultText);
            if (mr.value == expected) {
                bestMoveTexts.append(mr.move).append(" ");
            }
        }
        return bestMoveTexts.toString();
    }

    private static class MoveResult {
        final String move;
        final int value;

        MoveResult(String moveResultText) {
            final String[] parts = moveResultText.split("\\s*:\\s*");
            move = parts[0];
            value = Integer.parseInt(parts[1]);
        }
    }

    private static SolveResult SolveProblem(Searcher searcher, Problem problem) {
        final long t0 = System.currentTimeMillis();

        final Counts c0 = searcher.getCounts();

        final MoveScore moveScore;
        try {
            moveScore = searcher.getMoveScore(problem.board);
        } catch (SearchAbortedException e) {
            // this can never happen because we used AbortCheck.NEVER
            throw new IllegalStateException("Shouldn't be here.");

        }
        final int score = moveScore.centidisks / CoefficientCalculator.DISK_VALUE;

        final Counts counts = searcher.getCounts().minus(c0);

        final double seconds = 0.001 * (System.currentTimeMillis() - t0);

        final double mn = counts.nFlips * 1e-6;

        final boolean hasCorrectMove = problem.bestMoves.contains(BitBoardUtils.sqToText(moveScore.sq));
        System.out.format("%s  %s %+6.2f  %6.1fs  %d empty  %7.1f Mn    %4.1f Mn/s  %s\n", problem.problemName,
                BitBoardUtils.sqToText(moveScore.sq), score * 0.01, seconds, problem.board.nEmpty(), mn, mn / seconds
                , !hasCorrectMove ? "WRONG MOVE" : "");
        return new SolveResult(mn, seconds, hasCorrectMove ? 1 : 0);
    }

    private static class Problem {
        public final Board board;
        public final String bestMoves;
        public final String problemName;

        private Problem(Board board, String bestMoves, String problemName) {
            this.board = board;
            this.bestMoves = bestMoves;
            this.problemName = problemName;
        }
    }

    private static class SolveResult {
        final double megaNodes;
        final double seconds;
        final int nCorrectMoves;

        private SolveResult(double megaNodes, double seconds, int nCorrectMoves) {
            this.megaNodes = megaNodes;
            this.seconds = seconds;
            this.nCorrectMoves = nCorrectMoves;
        }
    }

    public interface Searcher {
        /**
         * Get the number of nodes this Searcher has visited since it was created
         *
         * @return number of nodes since this Searcher was created
         */
        @NotNull Counts getCounts();

        /**
         * Get the best move, and the score in disks
         *
         * @param board position to evaluate
         * @return best move and score
         */
        @NotNull MoveScore getMoveScore(Board board) throws SearchAbortedException;
    }

    public static class Midgame implements Searcher {
        private final MidgameSearcher searcher;

        public Midgame(MidgameSearcher searcher) {
            this.searcher = searcher;
        }

        @NotNull @Override public Counts getCounts() {
            return searcher.getCounts();
        }

        @NotNull @Override public MoveScore getMoveScore(Board board) throws SearchAbortedException {
            final int width = 0;
            return searcher.getMoveScore(board, board.calcMoves(), board.nEmpty(), width, AbortCheck.NEVER);
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

        @NotNull @Override public MoveScore getMoveScore(Board board) throws SearchAbortedException {
            final MoveScore moveScore = solver.getMoveScore(board.mover(), board.enemy(), AbortCheck.NEVER, StatsListener.NULL);
            return new MoveScore(moveScore.sq, moveScore.centidisks * CoefficientCalculator.DISK_VALUE);
        }

        @Override public String toString() {
            return "Solver";
        }
    }

    /**
     * A Searcher based on an external instance of NTest.
     * <p>
     * The location of the NTest executable is specified by the ExternalEngineManager using
     * program name "ntest".
     */
    public static class NtestSearcher implements Searcher {
        private final SyncPlayer player = createNtest();

        private static SyncPlayer createNtest() {
            final ExternalEngineManager.Xei xei = ExternalEngineManager.instance.getXei("ntest");
            if (xei == null) {
                System.out.println("ntest not available on this machine, skipping test");
                try {
                    List<ExternalEngineManager.Xei> engines = ExternalEngineManager.instance.getXei();
                    System.out.println("Available engines: ");
                    for (ExternalEngineManager.Xei engine : engines) {
                        System.out.println(engine);
                    }
                } catch (BackingStoreException e) {
                    System.err.println("massively messed up : " + e);
                }
                throw new IllegalStateException("ntest not available");
            }
            final NBoardSyncEngine ntest = new NBoardSyncEngine(xei, false);

            return new SyncPlayer(ntest, 50);
        }

        @NotNull @Override public Counts getCounts() {
            return new Counts(0, 0);
        }

        @NotNull @Override public MoveScore getMoveScore(Board board) {
            return player.calcMove(board);
        }

        @Override public String toString() {
            return "ntest";
        }
    }
}
