package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;
import com.welty.novello.core.BitBoardUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 */
public class EvalStrategy {
    private final String name;
    private final Term[] terms;
    private static final boolean debug = false;

    /**
     * Directory containing all coefficients
     */
    private static final Path rootDirectory = Paths.get("coefficients");

    /**
     * Distinct features of terms, in order of their first appearance in terms
     */
    private final Feature[] features;

    /**
     * terms[i].getFeature() == features[iFeatures[i]]
     */
    private final int[] iFeatures;

    EvalStrategy(String name, Term... terms) {
        this.name = name;
        this.terms = terms;
        iFeatures = new int[terms.length];

        final ArrayList<Feature> features = new ArrayList<>();
        for (int i = 0; i < terms.length; i++) {
            final Term term = terms[i];
            final Feature feature = term.getFeature();
            int iFeature = features.indexOf(feature);
            if (iFeature < 0) {
                iFeature = features.size();
                features.add(feature);
            }
            iFeatures[i] = iFeature;
        }
        this.features = features.toArray(new Feature[features.size()]);
        EvalStrategies.addStrategy(name, this);
    }

    /**
     * Read all coefficients for the strategy at a given nEmpty.
     * <p/>
     * This version reads from the default coefficient directory
     * <p/>
     * The file location is the same as in {@link #writeSlice(int, double[], String)}
     *
     * @param coeffSetName name of the coefficient set. This is used as a directory name so it can't contain
     *                     characters such as *?:
     * @return slice
     */
    int[][] readSlice(int nEmpty, String coeffSetName) {
        return readSlice(nEmpty, coeffDir(coeffSetName));
    }

    /**
     * Read all coefficients for the strategy at a given nEmpty.
     * <p/>
     * Coefficients for feature 0 are written first, then feature 1, etc.
     * Coefficients for each feature are indexed by orid.
     * <p/>
     * The file location is the same as in {@link #writeSlice(int, double[], String)}
     *
     * @param nEmpty               # of empties of file to read
     * @param coefficientDirectory location to read from
     */
    int[][] readSlice(int nEmpty, Path coefficientDirectory) {
        final Path path = coefficientDirectory.resolve(filename(nEmpty));
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            final int nFeatures = nFeatures();
            final int[][] slice = new int[nFeatures][];

            for (int iFeature = 0; iFeature < nFeatures; iFeature++) {
                final Feature feature = getFeature(iFeature);
                slice[iFeature] = readInts(in, feature.nOrids());
            }
            return slice;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int[] readInts(DataInputStream in, int nOrids) throws IOException {
        final int[] coeffsByOrid = new int[nOrids];
        for (int i = 0; i < nOrids; i++) {
            coeffsByOrid[i] = in.readInt();
        }
        return coeffsByOrid;
    }

    /**
     * Write a slice to disk, in the default location.
     * <p/>
     * The input coefficients are a double[] rather than the more common int[][].
     * The file format is simply a list of integers back-to-back; this converts the doubles to ints and
     * writes them out.
     * <p/>
     * The file location is {rootDirectory}/{eval name}/{coeffSetName}/{nEmpty}.coeff
     *
     * @param nEmpty       # of empties on board for this slice
     * @param coefficients coefficients, as a double[]
     * @param coeffSetName name of the coefficient set. This is used as a directory name so it can't contain
     *                     characters such as *?:
     * @throws java.io.IOException if can't open file output stream
     */
    void writeSlice(int nEmpty, double[] coefficients, String coeffSetName) throws IOException {
        writeSlice(nEmpty, coefficients, coeffDir(coeffSetName));
    }

    /**
     * @param coeffSetName name of the coefficient set. This is used as a directory name. So that it works on all
     *                     systems it is required to be alphanumeric.
     * @return location where this coeffSet is stored
     */
    private Path coeffDir(String coeffSetName) {
        if (coeffSetName.matches("[a-zA-Z0-9]+")) {
            return rootDirectory.resolve(name).resolve(coeffSetName);
        } else {
            throw new IllegalArgumentException("Coeff set name must be alphanumeric, was " + coeffSetName);
        }
    }

    /**
     * Checks that at least one slice can be created.
     * <p/>
     * A slice can be created if the coeffSetName is alphanumeric and either:
     * The coeffSet dir does not exist, or
     * The coeffSet dir is a directory which is missing slices.
     *
     * @param coeffSetName name of the coefficient set. This is used as a directory name. So that it works on all
     *                     systems it is required to be alphanumeric.
     * @throws IllegalArgumentException if no slices can be created.
     */
    public void checkSlicesCanBeCreated(String coeffSetName) {
        final Path dir = coeffDir(coeffSetName);
        if (!Files.exists(dir)) {
            return;
        }
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Can't create coefficient directory " + dir + " - a file with that name already exists");
        }
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            if (!Files.exists(dir.resolve(filename(nEmpty)))) {
                return;
            }
        }
        throw new IllegalArgumentException("All slices for " + coeffSetName + " have already been calculated.");
    }

    /**
     * Write a slice to disk.
     * <p/>
     * This function will not allow files to be overwritten, because coefficient sets need to be stable so that experiments can
     * be repeated.
     *
     * @param nEmpty       # of empties this slice is for
     * @param coefficients coefficients for the slice
     * @param dir          directory of the coefficient set, typically as described in {@link #writeSlice(int, double[], String)}
     * @throws IOException
     */
    void writeSlice(int nEmpty, double[] coefficients, Path dir) throws IOException {
        Require.eq(coefficients.length, "# coefficients", nCoefficientIndices());
        final Path path = dir.resolve(filename(nEmpty));
        if (Files.exists(path)) {
            throw new IllegalArgumentException("Coefficient set already exists: " + path);
        }

        Files.createDirectories(dir);

        int nNonZero = 0;
        try (final DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
            for (double c : coefficients) {
                final int intCoeff = (int) Math.round(c * CoefficientCalculator.DISK_VALUE);
                if (intCoeff != 0) {
                    nNonZero++;
                }
                out.writeInt(intCoeff);
            }
        }
        System.out.println(nNonZero + " non-zero coefficients written");
    }

    /**
     * Are there coefficients on disk for this slice?
     *
     * @param coeffSetName name of coeff set being checked
     * @param nEmpty # of empty disks for this slice
     * @return true if coefficients exist for this slice
     */
    boolean sliceExists(String coeffSetName, int nEmpty) {
        return Files.exists(coeffDir(coeffSetName).resolve(filename(nEmpty)));
    }

    private static String filename(int nEmpty) {
        return nEmpty + ".coeff";
    }

    /**
     * Calculate coefficient indices for the position.
     * <p/>
     * There is one coefficient index for each Term in this EvalStrategy.
     *
     * @param mover mover disks
     * @param enemy enemy disks
     * @return coefficient calculator indices for the position
     */
    public int[] coefficientIndices(long mover, long enemy) {
        final int[] coefficientIndices = new int[terms.length];

        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        final int[] coefficientIndexStarts = Vec.accumulate0(nOridsByFeature());
        for (int iTerm = 0; iTerm < terms.length; iTerm++) {
            final int orid = terms[iTerm].orid(mover, enemy, moverMoves, enemyMoves);
            final int iFeature = iFeatures[iTerm];
            final int coefficientIndex = orid + coefficientIndexStarts[iFeature];
            coefficientIndices[iTerm] = coefficientIndex;
        }
        return coefficientIndices;
    }

    /**
     * Evaluate a position.
     * <p/>
     * Precondition: mover can move. It is the caller's job to switch sides if no moves are available.
     *
     * @return position value, in centi-disks
     */
    int eval(long mover, long enemy, long moverMoves, long enemyMoves, CoefficientSet coefficientSet) {
        assert moverMoves != 0;

        final int[][] slice = coefficientSet.slice(BitBoardUtils.nEmpty(mover, enemy));

        int eval = 0;

        for (int iTerm = 0; iTerm < terms.length; iTerm++) {
            final Term term = terms[iTerm];
            final int iFeature = iFeatures[iTerm];

            final int orid = term.orid(mover, enemy, moverMoves, enemyMoves);

            final int coeff = slice[iFeature][orid];
            eval += coeff;
        }
        return eval;
    }


    void explain(long mover, long enemy, long moverMoves, long enemyMoves, CoefficientSet coefficientSet) {
        assert moverMoves != 0;

        final int[][] slice = coefficientSet.slice(BitBoardUtils.nEmpty(mover, enemy));

        int eval = 0;

        for (int iTerm = 0; iTerm < terms.length; iTerm++) {
            final Term term = terms[iTerm];
            final int iFeature = iFeatures[iTerm];

            final int orid = term.orid(mover, enemy, moverMoves, enemyMoves);

            final int coeff = slice[iFeature][orid];
            final Feature feature = getFeature(iFeature);
            System.out.println(feature + "[" + feature.oridDescription(orid) + "] = " + coeff);
            eval += coeff;
        }
        System.out.println("total eval = " + eval);
    }

    /**
     * Calculate the total number of possible orids for all features.
     *
     * @return nOrids[iFeature] = the number of orids for that feature.
     */
    public int[] nOridsByFeature() {
        final int[] nOrids = new int[features.length];
        for (int i = 0; i < features.length; i++) {
            nOrids[i] = features[i].nOrids();
        }
        return nOrids;
    }

    /**
     * Print out the coefficients in human-readable form, with descriptions
     * <p/>
     * This version takes coefficients as they are stored in the CoeffSet.
     *
     * @param slice    coefficients to print
     * @param minValue minimum value of coefficients to print
     */
    public void dumpCoefficients(int[][] slice, int minValue) {
        Require.eq(slice.length, "slice length", nFeatures());
        for (int iFeature = 0; iFeature < nFeatures(); iFeature++) {
            final Feature feature = getFeature(iFeature);
            Features.dumpCoefficients(feature, slice[iFeature], minValue);
        }
    }

    public int nFeatures() {
        return features.length;
    }

    public Feature getFeature(int iFeature) {
        return features[iFeature];
    }

    public int nCoefficientIndices() {
        return Vec.sum(nOridsByFeature());
    }

    @Override public String toString() {
        return name;
    }
}