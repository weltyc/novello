package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;
import com.welty.novello.solver.BitBoardUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 */
class EvalStrategy {
    private final String name;
    private final Term[] terms;

    /**
     * Location of coefficient directory
     */
    static final Path defaultCoefficientDirectory = Paths.get("coefficients");

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
     *
     * @return slice
     */
    int[][] readSlice(int nEmpty) {
        return readSlice(nEmpty, defaultCoefficientDirectory);
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
     *
     * This happens in-place; each element of the slice array is replaced by a longer int[].
     * @param slice slice to decompress
     */
    void decompressSlice(int[][] slice) {
        for (int iFeature = 0; iFeature<nFeatures(); iFeature++) {
            final Feature feature = getFeature(iFeature);
            slice[iFeature]=coeffsByInstance(slice[iFeature], feature);
        }
    }

    /**
     * Read coefficients from a file.
     *
     * The "compressed" means that the index into the slice data is an orid rather than an instance.
     * Since there are fewer orids than instances, this leads to less data.
     *
     * @param nEmpty # of empties of file to read
     * @param coefficientDirectory location to read from
     * @return  compressed slice.
     */
    int[][] readCompressedSlice(int nEmpty, Path coefficientDirectory) {
        final String filename = getFilename(nEmpty);
        final Path path = coefficientDirectory.resolve(filename);
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

    /**
     * Convert coeffsByOrid (as read from a file) to coeffsByInstance (as used in the eval)
     *
     * @param coeffsByOrid array containing coefficients for each orid
     * @param feature      feature to map instances to orids
     * @return array containing coefficients for each instance.
     */
    private static int[] coeffsByInstance(int[] coeffsByOrid, Feature feature) {
        final int n = feature.nInstances();
        final int[] coeffsByInstance = new int[n];
        for (int i = 0; i < n; i++) {
            coeffsByInstance[i] = coeffsByOrid[feature.orid(i)];
        }
        return coeffsByInstance;
    }

    private static int[] readInts(DataInputStream in, int nOrids) throws IOException {
        final int[] coeffsByOrid = new int[nOrids];
        for (int i = 0; i < nOrids; i++) {
            coeffsByOrid[i] = in.readInt();
        }
        return coeffsByOrid;
    }

    /**
     * Write a slice to file.
     * <p/>
     * The input coefficients are a double[] rather than the more common int[][].
     * The file format is simply a list of integers back-to-back; this converts the doubles to ints and
     * writes them out.
     *
     * @param nEmpty       # of empties on board for this slice
     * @param coefficients coefficients, as a double[]
     * @throws java.io.IOException if can't open file output stream
     */
    void writeSlice(int nEmpty, double[] coefficients) throws IOException {
        writeSlice(nEmpty, coefficients, defaultCoefficientDirectory);
    }

    void writeSlice(int nEmpty, double[] coefficients, Path coefficientDirectory) throws IOException {
        Require.eq(coefficients.length, "# coefficients", nCoefficientIndices());
        Files.createDirectories(coefficientDirectory);
        final String filename = getFilename(nEmpty);
        final Path path = coefficientDirectory.resolve(filename);
        try (final DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
            for (double c : coefficients) {
                final int intCoeff = (int) Math.round(c);
                if (intCoeff != 0) {
                    System.out.print("^");
                }
                out.writeInt(intCoeff);
            }
            System.out.println();
        }
    }

    String getFilename(int nEmpty) {
        return name + "_" + nEmpty + ".coeff";
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

    public int eval(long mover, long enemy, CoefficientSet coefficientSet) {
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        final int[][] slice = coefficientSet.slice(BitBoardUtils.nEmpty(mover, enemy));

        int eval = 0;

        for (int iTerm = 0; iTerm < terms.length; iTerm++) {
            final Term term = terms[iTerm];
            final int iFeature = iFeatures[iTerm];

            final int orid = term.orid(mover, enemy, moverMoves, enemyMoves);

            eval += slice[iFeature][orid];
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
     * This version takes coefficients as they are produced by the coefficient calculator.
     *
     * @param coefficients coefficients to print
     */
    public void dumpCoefficients(double[] coefficients) {
        Require.eq(coefficients.length, "# coefficients", nCoefficientIndices());
        for (int i = 0; i < coefficients.length; i++) {
            System.out.format("%5.1f  %s%n", coefficients[i], terms[0].getFeature().oridDescription(i));
        }
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
        for (int i = 0; i < nFeatures(); i++) {
            Require.eq(slice[i].length, "slice[" + i + "].length", getFeature(i).nInstances());
        }
        for (int iFeature = 0; iFeature < nFeatures(); iFeature++) {
            final Feature feature = getFeature(iFeature);
            final int[] coefficients = slice[iFeature];
            for (int instance = 0; instance < coefficients.length; instance++) {
                final int coefficient = coefficients[iFeature];
                if (coefficient != 0) {
                    final String desc = feature.oridDescription(feature.orid(instance));
                    System.out.format("%4d  %s%n", coefficient, desc);
                }
            }
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
}

/**
 * Utility class containing EvalStrategy instances
 */
class EvalStrategies {
    @SuppressWarnings("OctalInteger")
    static final EvalStrategy eval1 = new EvalStrategy("Corner",
            new CornerTerm(000),
            new CornerTerm(007),
            new CornerTerm(070),
            new CornerTerm(077)
    );

    static final EvalStrategy edgeEval = new EvalStrategy("Diagonals",
            new ULDRTerm(),
            new URDLTerm()
    );
}