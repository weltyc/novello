package com.welty.novello.eval;

import com.orbanova.common.math.function.oned.Function;
import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Utils;
import com.orbanova.common.misc.Vec;
import com.welty.novello.coca.ConjugateGradientMethod;
import com.welty.novello.coca.FunctionWithGradient;
import com.welty.novello.core.*;
import com.welty.novello.selfplay.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 */
public class CoefficientCalculator {
    private static final Logger log = Logger.logger(CoefficientCalculator.class);

    /**
     * 1 disk is worth how many evaluation points?
     */
    public static final int DISK_VALUE = 100;
    private static final String target = "c5s";
    private static final EvalStrategy STRATEGY = EvalStrategies.strategy(target.substring(0, 1));
    private static final String COEFF_SET_NAME = target.substring(1);
    private static final double PENALTY = 100;
    private static final String PLAYOUT_PLAYER_NAME = "c4s:4";

    static final int[] nEmpties = {3, 4, 11, 12, 19, 20, 27, 28, 35, 36, 43, 44, 51, 52, 59, 60};

    /**
     * Generate coefficients for evaluation.
     * <p/>
     * This program generates a large number of PositionValues by self-play, then uses
     * those values to generate coefficients for the Evaluation function.
     * <p/>
     * With no arguments, this generates coefficients for the target EvalStrategy, but only for those slices that don't
     * already exist. If no slices can be generated, the program terminates early.
     * <p/>
     * With -h, display orid histograms without generating coefficients. This displays histograms regardless of whether
     * coefficients have already been generated.
     */
    public static void main(String[] args) throws Exception {
        final String shortOptions = NovelloUtils.getShortOptions(args);
        final boolean histogramOnly = shortOptions.contains("h");

        if (histogramOnly) {
            System.out.println("Not generating coefficients - histograms only");
        }

        // better to learn this now than after all the computations
        if (!histogramOnly) {
            try {
                STRATEGY.checkSlicesCanBeCreated(COEFF_SET_NAME);
            } catch (IllegalArgumentException e) {
                log.warn("Coefficient set already exists.\n\nOverwriting coefficient files is not allowed.");
                System.exit(-1);
            }
        }

        final List<PositionValue> pvs = loadOrCreatePvsx();

        System.out.format("a total of %,d pvs are available.%n", pvs.size());
        System.out.println();
        for (int nEmpty : nEmpties) {
            if (!histogramOnly && STRATEGY.sliceExists(COEFF_SET_NAME, nEmpty)) {
                continue;
            }
            System.out.println("--- " + nEmpty + " ---");
            final PositionElement[] elements = elementsFromPvs(pvs, nEmpty);
            dumpElementDistribution(elements, STRATEGY.nCoefficientIndices());
            if (!histogramOnly) {
                System.out.format("estimating coefficients using %,d positions\n", elements.length);
                final long t0 = System.currentTimeMillis();
                final double[] x = estimateCoefficients(elements, STRATEGY.nCoefficientIndices(), STRATEGY.nDenseWeights, PENALTY);
                final double[] coefficients = STRATEGY.unpack(x);
                final long dt = System.currentTimeMillis() - t0;
                System.out.format("%,d ms elapsed\n", dt);
                System.out.format("sum of coefficients squared = %.3g\n", Vec.sumSq(coefficients));
                System.out.println();

                // write to file
                STRATEGY.writeSlice(nEmpty, coefficients, COEFF_SET_NAME);
            }
        }
    }


    private static void dumpElementDistribution(PositionElement[] elements, int nIndices) {
        new OridHistogram(elements, nIndices).dump();
    }

    public static List<PositionValue> loadOrCreatePvsx() throws IOException {
        final String playerComponent = PLAYOUT_PLAYER_NAME.replace(':', '-');
        final List<PositionValue> pvs = loadOrCreatePvs(playerComponent);

        final Path cacheDir = getCacheDir();
        final Path pvxFile = cacheDir.resolve(playerComponent + "x.pvs");
        if (!Files.exists(pvxFile)) {
            final Player PLAYOUT_PLAYER = Players.player(PLAYOUT_PLAYER_NAME);
            createPvx(pvxFile, pvs, PLAYOUT_PLAYER);
        }
        final List<PositionValue> pvx = loadPvs(pvxFile);
        log.info(String.format("%,d pvs from pv file and %,d pvs from pvx file\n", pvs.size(), pvx.size()));
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
     * @param elements      indices for each position to match
     * @param nCoefficients number of coefficients
     * @param nDenseWeights number of dense weights
     * @return optimal coefficients
     */
    static double[] estimateCoefficients(PositionElement[] elements, int nCoefficients, int nDenseWeights, double penalty) {
        final FunctionWithGradient f = new ErrorFunction(elements, nCoefficients, nDenseWeights, penalty);
        return ConjugateGradientMethod.minimize(f);
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
     * The function we are trying to minimize.
     * <p/>
     * This function is the sum of two components: the sum of squared errors plus
     * a PENALTY term which forces the coefficients to 0.
     * <p/>
     * Let
     * <ul>
     * <li>i be an index into the coefficient array</li>
     * <li>x[i] be the coefficient for index i</li>
     * <li>e be an element in the elements list</li>
     * <li>e.N[i] be the number of times pattern i occurs in the element</li>
     * <li>p be the size of the PENALTY</li>
     * </ul>
     * An element's error, e.error = e.target - &Sigma;<sub>i</sub>e.N[i]x[i]
     * <p/>
     * The sum of squared errors is<br/>
     * &Sigma;<sub>e</sub>e.error^2<br/>
     * <p/>
     * Its gradient for index i is
     * -2 &Sigma;<sub>e</sub>error*e.N[i]
     * <p/>
     * The PENALTY term is<br/>
     * p &Sigma;<sub>i</sub>x[i]^2
     * <p/>
     * Its gradient for index i is
     * 2 p x[i]
     */
    static class ErrorFunction extends FunctionWithGradient {
        private final PositionElement[] elements;
        private final int nCoefficients;
        private final int nDenseWeights;
        private final double penalty;

        public ErrorFunction(PositionElement[] elements, int nCoefficients, int nDenseWeights, double penalty) {
            this.elements = elements;
            this.nCoefficients = nCoefficients;
            this.nDenseWeights = nDenseWeights;
            this.penalty = penalty;
        }

        @Override
        public double[] minusGradient(double[] x) {
            Require.eq(x.length, "x length", nDimensions());
            final double[] minusGradient = Vec.times(x, -2 * penalty);
            for (final PositionElement element : elements) {
                element.updateGradient(x, minusGradient);
            }
            return minusGradient;
        }

        @Override
        public double y(double[] x) {
            Require.eq(x.length, "x length", nDimensions());
            double y = 0;
            for (PositionElement element : elements) {
                final double error = element.error(x);
                y += error * error;
            }
            return y + penalty * Vec.sumSq(x);
        }

        @Override
        public int nDimensions() {
            return nCoefficients + nDenseWeights;
        }

        @NotNull
        public @Override Function getLineFunction(double[] x, double[] dx) {
            return new LineFunction(x, dx);
        }

        /**
         * Precomputes some stuff to speed up line minimization
         */
        class LineFunction implements Function {
            final double[] errors;
            final double[] dErrors;
            private final double[] x;
            private final double[] dx;

            public LineFunction(double[] x, double[] dx) {
                this.x = x;
                this.dx = dx;
                errors = new double[elements.length];
                dErrors = new double[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    final PositionElement element = elements[i];
                    errors[i] = element.error(x);
                    dErrors[i] = element.dError(dx);
                }
            }

            @Override
            public double y(double a) {
                double y = 0;
                for (int i = 0; i < errors.length; i++) {
                    final double error = errors[i] + dErrors[i] * a;
                    y += error * error;
                }
                double p = 0;
                for (int i = 0; i < x.length; i++) {
                    final double coeff = x[i] + a * dx[i];
                    p += coeff * coeff;
                }
                return y + penalty * p;
            }
        }
    }

    /**
     * Select the pvs that will be used to generate coefficients at the given number of nEmpties and generate their Elements
     *
     * @param pvs    list of pvs at all empties
     * @param nEmpty number of empties to generate coefficients for
     * @return list of selected Elements
     */
    private static PositionElement[] elementsFromPvs(List<PositionValue> pvs, int nEmpty) {
        final List<PositionElement> res = new ArrayList<>();
        for (final PositionValue pv : pvs) {
            final int diff = nEmpty - pv.nEmpty();
            if (!Utils.isOdd(diff) && diff >= -6 && diff <= 6) {
                final PositionElement element = STRATEGY.coefficientIndices(pv.mover, pv.enemy, pv.value);
                res.add(element);
            }
        }
        return res.toArray(new PositionElement[res.size()]);
    }

    /**
     * Generate a set of PositionValues for analysis and write them to a file.
     *
     * @param pvFile path to the file to be written.
     */
    private static void createPvs(Path pvFile, Player playoutPlayer) throws IOException {
        log.info("Creating Pvs in " + pvFile + " ...");
        final List<Mr> mrs = getOrCreateMrs(playoutPlayer);
        Files.createDirectories(pvFile.getParent());
        try (final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(pvFile)))) {
            writeSubpositions(mrs, playoutPlayer, out);
        }
    }

    private static List<Mr> getOrCreateMrs(Player playoutPlayer) throws IOException {
        final Path mrsPath = getCacheDir().resolve("base.mrs");
        if (!Files.exists(mrsPath)) {
            final Set<Mr> mrSet = new HashSet<>();
            Files.createDirectories(mrsPath.getParent());
            final List<PositionValue> pvs = new SelfPlaySet(playoutPlayer, playoutPlayer, 0, false).call().pvs;
            for (PositionValue pv : pvs) {
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


    private static void createPvx(Path pvxFile, List<PositionValue> pvs, Player playoutPlayer) throws IOException {
        log.info("Creating Pvx in " + pvxFile + " ...");
        final Set<Mr> rares = generateRareSubpositions(STRATEGY, pvs);
        try (ProgressUpdater pu = new ProgressUpdater("Create pvx", rares.size())) {
            Files.createDirectories(pvxFile.getParent());
            final HashSet<Mr> alreadyWritten = new HashSet<>();
            try (final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(pvxFile)))) {
                int progress = 0;
                for (Mr rare : rares) {
                    writeFirstTwoPvs(out, playoutPlayer, rare.toPosition(), alreadyWritten);
                    progress++;
                    if ((progress & 0xFFF) == 0) {
                        pu.setProgress(progress);
                    }
                }
            }
        }
    }


    /**
     * Value all positions and write out their pvs.
     * <p/>
     * For all positions in the list, play a game from that
     * position and append the first two positions from that game to out.
     * <p/>
     * The positions are valued by the playout. The playout is played by eval4/A, which is the current best
     * evaluator.
     * <p/>
     * This function does NOT close the DataOutputStream.
     *
     * @param mrs positions that might get chosen
     */
    private static void writeSubpositions(List<Mr> mrs, Player player, DataOutputStream out) throws IOException {
        int nextMessage = 50000;
        int nWritten = 0;

        final HashSet<Mr> alreadyWritten = new HashSet<>();

        log.info(String.format("%,d original positions", mrs.size()));
        for (Mr mr : mrs) {
            nWritten += writeSubPositions(out, mr.mover, mr.enemy, player, alreadyWritten);
            if (nWritten >= nextMessage) {
                log.info(String.format(" Added %,d sub-positions", nWritten));
                nextMessage += 50000;
            }
        }
    }

    /**
     * Write the first two positions of this game to out.
     *
     * @return number of positions written
     */
    private static int writeSubPositions(DataOutputStream out, long mover, long enemy, Player player, HashSet<Mr> alreadyWritten) throws IOException {
        int nWritten = 0;
        final Position pos = new Position(mover, enemy, true);
        long moves = pos.calcMoves();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            moves ^= 1L << sq;
            final Position subPos = pos.play(sq);
            nWritten += writeFirstTwoPvs(out, player, subPos, alreadyWritten);
        }
        return nWritten;
    }

    /**
     * Value the position and write both it and its successor position to file as PositionValues.
     * <p/>
     * This implementation values using a playout.
     *
     * @param out            Stream to write PositionValues to.
     * @param player         player to value a position.
     * @param position       the position
     * @param alreadyWritten list of Mrs that have already been written to file
     * @return number of PVs written. 2 unless the game ends after the first move.
     * @throws IOException
     */
    private static int writeFirstTwoPvs(DataOutputStream out, Player player, Position position, HashSet<Mr> alreadyWritten) throws IOException {
        final MutableGame game = new SelfPlayGame(position, player, player, "", 0, 0).call();
        final List<PositionValue> gamePvs = game.calcPositionValues();
        final List<PositionValue> toAdd = gamePvs.subList(0, Math.min(2, gamePvs.size()));
        for (PositionValue pv : toAdd) {
            final Mr mr = new Mr(pv.mover, pv.enemy);
            if (alreadyWritten.add(mr)) {
                pv.write(out);
            }
        }
        return toAdd.size();
    }

}

