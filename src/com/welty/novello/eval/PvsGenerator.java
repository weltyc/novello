package com.welty.novello.eval;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.*;
import com.welty.novello.selfplay.Player;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SelfPlayGame;
import com.welty.novello.selfplay.SelfPlaySet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

/**
 * Generates and loads PositionValues for the Coefficient Calculator
 */
public class PvsGenerator {
    private static final Logger log = Logger.logger(PvsGenerator.class);
    private final EvalStrategy evalStrategy;
    private static final String PLAYOUT_PLAYER_NAME = "c5s:10";

    public PvsGenerator(EvalStrategy evalStrategy) {
        this.evalStrategy = evalStrategy;
    }

    public List<PositionValue> loadOrCreatePvsx() throws IOException {
        final String playerComponent = PLAYOUT_PLAYER_NAME.replace(':', '-');
        final List<PositionValue> pvs = loadOrCreatePvs(playerComponent);

        final Path cacheDir = getCacheDir();
        final Path pvxFile = cacheDir.resolve(playerComponent + "x.pvs");
        if (!Files.exists(pvxFile)) {
            createPvx(pvxFile, pvs);
        }
        final List<PositionValue> pvx = loadPvs(pvxFile);
        log.info(String.format("%,d pvs from pv file and %,d pvs from pvx file", pvs.size(), pvx.size()));
        pvs.addAll(pvx);

        log.info("Selecting distinct pvs");
        final Set<Mr> alreadySeen = new HashSet<>();
        final List<PositionValue> distinctPvs = new ArrayList<>();

        for (PositionValue pv : pvs) {
            final Mr mr = new Mr(pv.mover, pv.enemy);
            if (alreadySeen.add(mr)) {
                distinctPvs.add(pv);
            }
        }
        log.info(String.format("selected %,d distinct pvs from %,d total pvs", distinctPvs.size(), pvs.size()));
        return distinctPvs;
    }

    public static List<PositionValue> loadOrCreatePvs(String playerComponent) throws IOException {
        final Path cacheDir = getCacheDir();
        final Path pvFile = cacheDir.resolve(playerComponent + ".pvs");
        if (!Files.exists(pvFile)) {
            final Player PLAYOUT_PLAYER = Players.player(PLAYOUT_PLAYER_NAME);
            createPvs(pvFile, PLAYOUT_PLAYER);
        }
        return loadPvs(pvFile);
    }

    private static Path getCacheDir() {
        final boolean isMac = System.getProperty("os.name").startsWith("Mac OS");
        final Path cacheDir;
        if (isMac) {
            cacheDir = Paths.get(System.getProperty("user.home") + "/Library/Caches/" + "com.welty.novello");
        } else {
            cacheDir = Paths.get("c:/temp/novello");
        }
        return cacheDir;
    }

    private static List<PositionValue> loadPvs(Path pvFile) throws IOException {
        final int nPvs = (int) ((Files.size(pvFile) / 20) >> 20) + 1;

        try (final Monitor<PositionValue> monitor = new Monitor<>("Loading pvs from file", nPvs)) {
            final List<PositionValue> pvs;
            final long t0 = System.currentTimeMillis();
            pvs = new ObjectFeed<>(pvFile, PositionValue.deserializer).map(monitor).asList();
            // Ram is tight... free some up
            ((ArrayList) pvs).trimToSize();

            final long dt = System.currentTimeMillis() - t0;
            System.out.format("...  loaded %,d pvs in %.3f s\n", pvs.size(), dt * .001);
            return pvs;
        }
    }

    /**
     * Generate a set containing minimal reflections of all Mes that are
     * (a) subpositions of a position in positionValues
     * (b) rare, and
     * (c) not in positionValues
     *
     * @return set of minimal reflections
     */
    public static Set<Mr> generateRareSubpositions(EvalStrategy strategy, List<PositionValue> pvs) {
        log.info("Starting generateRareSubpositions()");

        final int[][] countSlices = new int[64][strategy.nCoefficientIndices()];

        final Set<Mr> original = new HashSet<>();
        for (PositionValue pv : pvs) {
            original.add(new Mr(pv.mover, pv.enemy));
        }

        log.info(String.format("collected source positions. %,d distinct positions from %,d pvs", original.size(), pvs.size()));

        final ProgressMonitor progressMonitor = new ProgressMonitor(null, "Generate rare subpositions", "", 0, pvs.size());

        final HashSet<Mr> mrs = new HashSet<>();
        for (int i = 0; i < pvs.size(); i++) {
            final PositionValue pv = pvs.get(i);

            final Mr mr = new Mr(pv.mover, pv.enemy);
            final Collection<Mr> subs = mr.subPositions();
            for (Mr sub : subs) {
                if (!original.contains(sub) && !mrs.contains(sub)) {
                    final PositionElement element = strategy.coefficientIndices(mr.mover, mr.enemy, 0);
                    final int[] counts = countSlices[mr.nEmpty()];
                    if (element.isRare(counts, 10)) {
                        mrs.add(sub);
                        element.updateHistogram(counts);
                    }
                }
            }
            if ((i & 0x3FFFF) == 0) {
                progressMonitor.setProgress(i);
                log.info((i >> 10) + "k pvs processed; " + (mrs.size() >> 10) + "k rare positions generated");
            }
        }
        log.info("A total of " + (mrs.size() >> 10) + "k rare positions were created.");
        progressMonitor.close();
        return mrs;
    }

    /**
     * Generate a set of PositionValues for analysis and write them to a file.
     *
     * @param pvFile path to the file to be written.
     */
    private static void createPvs(Path pvFile, Player playoutPlayer) throws IOException {
        log.info("Creating Pvs in " + pvFile + " ...");
        final List<Mr> mrs = getOrCreateMrs(playoutPlayer);
        final HashSet<Mr> subMrs = generateSubMrs(mrs);
        Files.createDirectories(pvFile.getParent());
        writePvs(pvFile, subMrs);
    }

    private static List<Mr> getOrCreateMrs(Player playoutPlayer) throws IOException {
        final Path mrsPath = getCacheDir().resolve("base.mrs");
        if (!Files.exists(mrsPath)) {
            final Set<Mr> mrSet = new HashSet<>();
            Files.createDirectories(mrsPath.getParent());
            final SelfPlaySet.PvCollector pvCollector = new SelfPlaySet.PvCollector();
            SelfPlaySet.run(playoutPlayer, playoutPlayer, pvCollector);
            for (PositionValue pv : pvCollector.pvs) {
                mrSet.add(new Mr(pv.mover, pv.enemy));
            }
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(mrsPath)))) {
                for (Mr mr : mrSet) {
                    mr.write(out);
                }
            }
            log.info(String.format("created %s with %,d mrs", mrsPath, mrSet.size()));
        }


        return new ObjectFeed<>(mrsPath, Mr.deserializer).asList();
    }

    private void createPvx(Path pvxFile, List<PositionValue> pvs) throws IOException {
        log.info("Creating Pvx in " + pvxFile + " ...");
        final Set<Mr> rares = generateRareSubpositions(evalStrategy, pvs);
        Files.createDirectories(pvxFile.getParent());
        writePvs(pvxFile, rares);
    }

    /**
     * Generate PVs for each Mr and its successor
     *
     * @param file location to write PVs.
     * @param mrs  MRs to use to generate PV.
     * @throws IOException
     */
    private static void writePvs(Path file, Set<Mr> mrs) throws IOException {
        try (final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
            int nextMessage = 50000;
            int nWritten = 0;

            final int nThreads = 4;
            log.info("Starting Pvs executor service with " + nThreads + " threads");
            final ExecutorCompletionService<List<PositionValue>> ecs = new ExecutorCompletionService<>(Executors.newFixedThreadPool(nThreads));

            log.info("Generating and writing pvs...");
            for (Mr mr : mrs) {
                ecs.submit(new PvsTask(mr));
            }

            for (int i = 0; i < mrs.size(); i++) {
                try {
                    final List<PositionValue> firstTwoPvs = ecs.take().get();
                    for (PositionValue pv : firstTwoPvs) {
                        pv.write(out);
                    }
                    nWritten += firstTwoPvs.size();
                    if (nWritten >= nextMessage) {
                        log.info(String.format("%,d positions written", nWritten));
                        nextMessage += 50000;
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

    private static HashSet<Mr> generateSubMrs(List<Mr> mrs) throws IOException {
        final HashSet<Mr> subMrs = new HashSet<>();

        log.info(String.format("%,d original positions", mrs.size()));
        log.info("Generating subpositions...");
        for (Mr mr : mrs) {
            addSubMrs(subMrs, mr);
        }
        log.info("Done generating subpositions");
        return subMrs;
    }

    /**
     * Add an Mr for each subPosition to a set.
     */
    private static void addSubMrs(Set<Mr> mrs, Mr mr) throws IOException {
        final Position pos = new Position(mr.mover, mr.enemy, true);
        long moves = pos.calcMoves();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            moves ^= 1L << sq;
            final Position subPos = pos.play(sq);
            mrs.add(new Mr(subPos.mover(), subPos.enemy()));
        }
    }

    /**
     * Value the position and return both it and its successor position as PositionValues.
     * <p/>
     * This implementation values using a playout.
     *
     * @param player   player to value a position.
     * @param position the position
     * @return number of PVs written. 2 unless the game ends after the first move.
     */
    private static List<PositionValue> getFirstTwoPvs(Player player, Position position) {
        if (true) {
            // generate value using a midgame search
            List<PositionValue> pvs = new ArrayList<>();
            if (!position.hasLegalMove()) {
                position = position.pass();
                if (!position.hasLegalMove()) {
                    return pvs;
                }
            }
            final MoveScore moveScore = player.calcMove(position, position.calcMoves(), 0);
            pvs.add(new PositionValue(position.mover(), position.enemy(), moveScore.score));
            position.play(moveScore.sq);
            int subScore = -moveScore.score;

            if (!position.hasLegalMove()) {
                position = position.pass();
                if (!position.hasLegalMove()) {
                    return pvs;
                }
                subScore = moveScore.score;
            }

            pvs.add(new PositionValue(position.mover(), position.enemy(), subScore));
            return pvs;
        } else {
            // generate using playout
            final MutableGame game = new SelfPlayGame(position, player, player, "", 0, 0).call();
            final List<PositionValue> gamePvs = game.calcPositionValues();
            return gamePvs.subList(0, Math.min(2, gamePvs.size()));
        }
    }

    private static class PvsTask implements Callable<List<PositionValue>> {
        private static final DefaultThreadLocal<Player> players = new DefaultThreadLocal<>(
                new Factory<Player>() {
                    @NotNull @Override public Player construct() {
                        return Players.player(PLAYOUT_PLAYER_NAME);
                    }
                }
        );

        private final @NotNull Mr mr;

        public PvsTask(@NotNull Mr mr) {
            this.mr = mr;
        }

        @Override public List<PositionValue> call() throws Exception {
            return getFirstTwoPvs(players.getOrCreate(), new Position(mr));
        }
    }
}
