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

package com.welty.novello.coca;

import com.orbanova.common.gui.Monitor;
import com.orbanova.common.misc.Logger;
import com.welty.novello.core.*;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SelfPlayGame;
import com.welty.novello.selfplay.SyncPlayer;
import com.welty.othello.core.OperatingSystem;
import com.welty.othello.gdk.OsClock;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

/**
 * A MvSource that produces Mes by evaluation, caching the data to improve performance.
 */
public class CachingMvSource implements MvSource {
    private static final Logger log = Logger.logger(CachingMvSource.class);

    private final String playerName;
    private final MrSource mrSource;
    private final String extension;

    /**
     * @param playerName player name, e.g. "c1:8"
     * @param mrSource   source of positions, if cached values don't exist
     * @param extension  Filename component. Should be distinct to distinguish this from other caches. e.g. .pvs or x.pvs
     */
    public CachingMvSource(String playerName, MrSource mrSource, String extension) {
        this.playerName = playerName;
        this.mrSource = mrSource;
        this.extension = extension;
    }

    @Override public List<MeValue> getMvs() throws IOException {
        return loadOrCreatePvs();
    }

    private List<MeValue> loadOrCreatePvs() throws IOException {
        final Path mvFile = getCacheFilePath();
        if (!Files.exists(mvFile)) {
            createMvs(mvFile);
        }
        return loadMvs(mvFile);
    }

    private Path getCacheFilePath() {
        final Path cacheDir = getCacheDir();
        return cacheDir.resolve(playerComponent() + extension);
    }

    final String playerComponent() {
        return playerName.replace(':', '-');
    }

    static Path getCacheDir() {
        final Path cacheDir;
        if (OperatingSystem.os == OperatingSystem.MACINTOSH) {
            cacheDir = Paths.get(System.getProperty("user.home") + "/Library/Caches/" + "com.welty.novello");
        } else {
            cacheDir = Paths.get("c:/temp/novello");
        }
        return cacheDir;
    }

    private static List<MeValue> loadMvs(Path mvFile) throws IOException {
        final int nMvs = (int) ((Files.size(mvFile) / 20) >> 20) + 1;

        try (final Monitor<MeValue> monitor = new Monitor<>("Loading pvs from file", nMvs)) {
            final List<MeValue> pvs;
            final long t0 = System.currentTimeMillis();
            pvs = new ObjectFeed<>(mvFile, MeValue.deserializer).map(monitor).asList();
            // Ram is tight... free some up
            ((ArrayList) pvs).trimToSize();

            final long dt = System.currentTimeMillis() - t0;
            System.out.format("...  loaded %,d pvs in %.3f s\n", pvs.size(), dt * .001);
            return pvs;
        }
    }

    /**
     * Generate a set of PositionValues for analysis and write them to a file.
     *
     * @param mvFile path to the file to be written.
     */
    private void createMvs(Path mvFile) throws IOException {
        log.info("Creating Pvs in " + mvFile + " ...");
        final HashSet<Mr> subMrs = new HashSet<>(mrSource.getMrs());
        Files.createDirectories(mvFile.getParent());
        writePvs(mvFile, subMrs);
    }

    /**
     * Generate PVs for each Mr and its successor
     *
     * @param file location to write PVs.
     * @param mrs  MRs to use to generate PV.
     * @throws IOException
     */
    private void writePvs(Path file, Set<Mr> mrs) throws IOException {
        log.info("Generating pvs for " + String.format("%,d", mrs.size()) + " mrs, each of which will generate 2 pvs (unless there is only one remaining move in the game)");
        try (final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
            int nextMessage = 25000;
            int nWritten = 0;

            final int nThreads = 4;
            log.info("Starting Pvs executor service with " + nThreads + " threads");
            final ExecutorCompletionService<List<MeValue>> ecs = new ExecutorCompletionService<>(Executors.newFixedThreadPool(nThreads));

            log.info("Generating and writing pvs to " + file.getFileName() + " ...");
            for (Mr mr : mrs) {
                ecs.submit(new PvsTask(mr));
            }

            for (int i = 0; i < mrs.size(); i++) {
                try {
                    final List<MeValue> firstTwoPvs = ecs.take().get();
                    for (MeValue pv : firstTwoPvs) {
                        pv.write(out);
                    }
                    nWritten += firstTwoPvs.size();
                    if (nWritten >= nextMessage) {
                        log.info(String.format("%,dk positions written", nWritten / 1000));
                        nextMessage *= 2;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    // this thread spec does not allow InterruptedExceptions.
                    // ExecutionException also indicates a programming error.
                    throw new RuntimeException(e);
                }
            }
            log.info("Done writing pvs");
        }
    }

    private final ThreadLocal<SyncPlayer> players = new ThreadLocal<SyncPlayer>() {
        @Override protected SyncPlayer initialValue() {
            return Players.player(playerName);
        }
    };

    /**
     * Value the position and return both it and its successor position as MeValues.
     * <p/>
     * This implementation values using a midgame search.
     *
     * @param syncPlayer syncPlayer to value a position.
     * @param board   the position
     * @return The two pvs
     */
    static List<MeValue> getFirstTwoPvsSearch(SyncPlayer syncPlayer, Board board) {
        // generate value using a midgame search
        List<MeValue> pvs = new ArrayList<>();
        if (!board.hasLegalMove()) {
            board = board.pass();
            if (!board.hasLegalMove()) {
                return pvs;
            }
        }
        final MoveScore moveScore = syncPlayer.calcMove(board);
        pvs.add(new MeValue(board.mover(), board.enemy(), moveScore.centidisks));

        board = board.play(moveScore.sq);
        int subScore = -moveScore.centidisks;

        if (!board.hasLegalMove()) {
            board = board.pass();
            if (!board.hasLegalMove()) {
                return pvs;
            }
            subScore = moveScore.centidisks;
        }

        pvs.add(new MeValue(board.mover(), board.enemy(), subScore));
        return pvs;
    }

    /**
     * Value the position and return both it and its successor position as MeValues.
     * <p/>
     * This implementation values using a midgame search.
     *
     * @param syncPlayer syncPlayer to value a position.
     * @param board   the position
     * @return The two pvs
     */
    static List<MeValue> getFirstTwoPvsPlayout(SyncPlayer syncPlayer, Board board) {
        // generate using playout
        final MutableGame game = new SelfPlayGame(board, syncPlayer, syncPlayer, OsClock.LONG, "", 0).call();
        final List<MeValue> gamePvs = game.calcPositionValues();
        return gamePvs.subList(0, Math.min(2, gamePvs.size()));
    }

    private class PvsTask implements Callable<List<MeValue>> {
        private final @NotNull Mr mr;

        public PvsTask(@NotNull Mr mr) {
            this.mr = mr;
        }

        @Override public List<MeValue> call() throws Exception {
            return getFirstTwoPvsSearch(players.get(), new Board(mr));
        }
    }


}
