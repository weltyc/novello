package com.welty.novello.coca;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.*;
import com.welty.novello.selfplay.SyncEngine;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SelfPlayGame;
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
        final boolean isMac = System.getProperty("os.name").startsWith("Mac OS");
        final Path cacheDir;
        if (isMac) {
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
                        log.info(String.format("%,dk positions written", nWritten/1000));
                        nextMessage *=2;
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

    private final ThreadLocal<SyncEngine> players = new ThreadLocal<SyncEngine>() {
        @Override protected SyncEngine initialValue() {
            return Players.player(playerName);
        }
    };

    /**
     * Value the position and return both it and its successor position as MeValues.
     * <p/>
     * This implementation values using a midgame search.
     *
     * @param syncEngine   syncEngine to value a position.
     * @param position the position
     * @return The two pvs
     */
    static List<MeValue> getFirstTwoPvsSearch(SyncEngine syncEngine, Position position) {
        // generate value using a midgame search
        List<MeValue> pvs = new ArrayList<>();
        if (!position.hasLegalMove()) {
            position = position.pass();
            if (!position.hasLegalMove()) {
                return pvs;
            }
        }
        final MoveScore moveScore = syncEngine.calcMove(position);
        pvs.add(new MeValue(position.mover(), position.enemy(), moveScore.score));

        position = position.play(moveScore.sq);
        int subScore = -moveScore.score;

        if (!position.hasLegalMove()) {
            position = position.pass();
            if (!position.hasLegalMove()) {
                return pvs;
            }
            subScore = moveScore.score;
        }

        pvs.add(new MeValue(position.mover(), position.enemy(), subScore));
        return pvs;
    }

    /**
     * Value the position and return both it and its successor position as MeValues.
     * <p/>
     * This implementation values using a midgame search.
     *
     * @param syncEngine   syncEngine to value a position.
     * @param position the position
     * @return The two pvs
     */
    static List<MeValue> getFirstTwoPvsPlayout(SyncEngine syncEngine, Position position) {
        // generate using playout
        final MutableGame game = new SelfPlayGame(position, syncEngine, syncEngine, "", 0).call();
        final List<MeValue> gamePvs = game.calcPositionValues();
        return gamePvs.subList(0, Math.min(2, gamePvs.size()));
    }

    private class PvsTask implements Callable<List<MeValue>> {
        private final @NotNull Mr mr;

        public PvsTask(@NotNull Mr mr) {
            this.mr = mr;
        }

        @Override public List<MeValue> call() throws Exception {
            return getFirstTwoPvsSearch(players.get(), new Position(mr));
        }
    }


}
