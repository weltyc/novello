package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;
import com.welty.novello.solver.BitBoardUtils;

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
    static final Path rootDirectory = Paths.get("coefficients");

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
     *
     * @param coefficientDirectory location of coefficient files
     * @return slice
     */
    int[][] readSlice(int nEmpty, Path coefficientDirectory) {
        final int[][] slice = readCompressedSlice(nEmpty, coefficientDirectory);
        decompressSlice(slice);
        return slice;
    }

    /**
     * Convert a compressed slice into a decompressed slice.
     * <p/>
     * This happens in-place; each element of the slice array is replaced by a longer int[].
     *
     * @param slice slice to decompress
     */
    void decompressSlice(int[][] slice) {
        for (int iFeature = 0; iFeature < nFeatures(); iFeature++) {
            final Feature feature = getFeature(iFeature);
            slice[iFeature] = Features.coeffsByInstance(feature, slice[iFeature]);
        }
    }

    /**
     * Read coefficients from a file.
     * <p/>
     * The "compressed" means that the index into the slice data is an orid rather than an instance.
     * Since there are fewer orids than instances, this leads to less data.
     * <p/>
     * The file location is the same as in {@link #writeSlice(int, double[], String)}
     *
     * @param nEmpty               # of empties of file to read
     * @param coefficientDirectory location to read from
     * @return compressed slice.
     */
    int[][] readCompressedSlice(int nEmpty, Path coefficientDirectory) {
        final Path path = coefficientDirectory.resolve(filename(nEmpty));
        try (DataInputStream in = new DataInputStream(Files.newInputStream(path))) {
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
     * @param coeffSetName name of the coefficient set. This is used as a directory name. So that it works on all
     *                     systems it is required to be alphanumeric.
     * @return true if the given coefficient set exists on disk
     * @throws IllegalArgumentException if  the coeffSetName is not alphanumeric
     */
    public boolean coefficientsExist(String coeffSetName) {
        final Path dir = coeffDir(coeffSetName);
        return Files.exists(dir) && Files.isDirectory(dir);
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

    static String filename(int nEmpty) {
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
    public int eval(long mover, long enemy, long moverMoves, long enemyMoves, CoefficientSet coefficientSet) {
        assert moverMoves != 0;

        final int[][] slice = coefficientSet.slice(BitBoardUtils.nEmpty(mover, enemy));

        int eval = 0;

        for (int iTerm = 0; iTerm < terms.length; iTerm++) {
            final Term term = terms[iTerm];
            final int iFeature = iFeatures[iTerm];

            final int orid = term.orid(mover, enemy, moverMoves, enemyMoves);

            final int coeff = slice[iFeature][orid];
            if (debug) {
                final Feature feature = getFeature(iFeature);
                System.out.println(feature + "[" + feature.oridDescription(orid) + "] = " + coeff);
            }
            eval += coeff;
        }
        return eval;
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
     * @param slice coefficients to print
     */
    public void dumpCoefficients(int[][] slice) {
        Require.eq(slice.length, "slice length", nFeatures());
        for (int iFeature = 0; iFeature < nFeatures(); iFeature++) {
            final Feature feature = getFeature(iFeature);
            Features.dumpCoefficients(feature, slice[iFeature]);
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