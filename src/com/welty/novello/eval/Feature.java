package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;

import java.util.ArrayList;
import java.util.List;

/**
 */
interface Feature {
    /**
     * @return Number of orids (distinct instances) for this feature
     */
    int nOrids();

    /**
     * @return String description of the orid
     */
    String oridDescription(int orid);

    /**
     * @return Number of instances for this feature
     */
    int nInstances();

    /**
     * @return equivalence class of the instance
     */
    int orid(int instance);
}

/**
 * Utility class containing Features that can be used for evaluation
 */
class Features {
    static final SoloFeature cornerFeature = new SoloFeature("Corner Mobility",
            "No access to corner",
            "Mover access to corner",
            "Enemy access to corner",
            "Both access to corner",
            "Mover occupies corner",
            "Enemy occupies corner"
    );

    static final SoloFeature corner2Feature = new SoloFeature("Corner Mobility",
            "No access to corner",
            "Mover access to corner",
            "Enemy access to corner",
            "Both access to corner",
            "Mover occupies corner",
            "Enemy occupies corner",
            "Mover x-square",
            "Enemy x-square"
    );

    static final SoloFeature moverDisks = new GridFeature("Mover Disks");
    static final SoloFeature enemyDisks = new GridFeature("Enemy Disks");
    static final SoloFeature moverMobilities = new GridFeature("Mover Mobilities");
    static final SoloFeature enemyMobilities = new GridFeature("Enemy Mobilities");
    static final SoloFeature moverPotMobs = new GridFeature("Mover PotMobs");
    static final SoloFeature enemyPotMobs = new GridFeature("Enemy PotMobs");


    static final Feature edgeFeature = LinePatternFeatureFactory.of("edges", 8);
    static final Feature mainDiagonalFeature = LinePatternFeatureFactory.of("main diagonal", 8);

    /**
     * Convert coeffsByOrid (as read from a file) to coeffsByInstance (as used in the eval)
     *
     * @param feature      feature to map instances to orids
     * @param coeffsByOrid array containing coefficients for each orid
     * @return array containing coefficients for each instance.
     */
    static int[] coeffsByInstance(Feature feature, int[] coeffsByOrid) {
        final int n = feature.nInstances();
        final int[] coeffsByInstance = new int[n];
        for (int i = 0; i < n; i++) {
            coeffsByInstance[i] = coeffsByOrid[feature.orid(i)];
        }
        return coeffsByInstance;
    }

    /**
     * Print a human-readable description of the coefficients to System.out
     *
     * @param feature      feature used to interpret the coefficients
     * @param coefficients coefficients to print
     * @param minValue     minimum absolute value to print a coefficient
     */
    static void dumpCoefficients(Feature feature, int[] coefficients, int minValue) {
        Require.eq(coefficients.length, "# coefficients", feature.nInstances());
        boolean[] printedOrids = new boolean[feature.nOrids()];

        System.out.println();
        System.out.println(feature + ":");
        int nLarge = 0;
        for (int instance = 0; instance < coefficients.length; instance++) {
            final int coefficient = coefficients[instance];
            if (Math.abs(coefficient) >= minValue) {
                final int orid = feature.orid(instance);
                if (!printedOrids[orid]) {
                    final String desc = feature.oridDescription(orid);
                    System.out.format("%+5d  %s (i=%d, o=%d)%n", coefficient, desc, instance, orid);
                    printedOrids[orid]=true;
                }
                nLarge++;
            }
        }
        System.out.println("(" + nLarge + " coefficients valued at least " + minValue + " out of " + feature.nInstances() + " total coefficients)");
    }
}


/**
 * A feature that combines multiple instances into a single orid via lookup table
 */
class MultiFeature implements Feature {
    private final int[] orids;
    private final String[] oridDescriptions;
    private final String name;

    public MultiFeature(String name, int[] orids, String[] oridDescriptions) {
        this.name = name;
        Require.lt(Vec.max(orids), "maximum orid value", oridDescriptions.length, "number of orid descriptions");
        this.orids = orids;
        this.oridDescriptions = oridDescriptions;
    }

    @Override public int nOrids() {
        return oridDescriptions.length;
    }

    @Override public String oridDescription(int orid) {
        return oridDescriptions[orid];
    }

    @Override public int nInstances() {
        return orids.length;
    }

    @Override public int orid(int instance) {
        return orids[instance];
    }

    @Override public String toString() {
        return name;
    }
}

/**
 * A Feature that has a 1-to-1 mapping between instances and orids.
 */
class SoloFeature implements Feature {
    private final String name;
    private final String[] oridDescriptions;

    public SoloFeature(String name, String... oridDescriptions) {
        this.name = name;
        this.oridDescriptions = oridDescriptions;
    }

    @Override public int orid(int instance) {
        return instance;
    }

    @Override public int nOrids() {
        return oridDescriptions.length;
    }

    @Override public String oridDescription(int orid) {
        return oridDescriptions[orid];
    }

    @Override public int nInstances() {
        return nOrids();
    }

    @Override public String toString() {
        return name;
    }
}

class GridFeature extends SoloFeature {
    public GridFeature(String name) {
        super(name, grid(name));
    }


    private static String[] grid(String name) {
        final String[] result = new String[65];
        for (int i = 0; i <= 64; i++) {
            result[i] = String.format("%2d %s", i, name);
        }
        return result;
    }
}

/**
 * A Feature that uses, as its instance, a base-3 representation of the disks in a line.
 * <p/>
 * 0 = empty, 1=mover, 2=enemy.
 * Any disk pattern gives the same orid as reversing its disks.
 * <p/>
 * Disk patterns are displayed assuming black is the mover; * = mover, O = enemy.
 */
class LinePatternFeatureFactory {
    static Feature of(String name, int nDisks) {
        final int[] orids = new int[Base3.nInstances(nDisks)];
        final List<String> oridDescList = new ArrayList<>();

        int nOrids = 0;
        for (int instance = 0; instance < orids.length; instance++) {
            final int reverse = Base3.reverse(instance, nDisks);
            if (reverse < instance) {
                orids[instance] = orids[reverse];
            } else {
                oridDescList.add(Base3.description(instance, nDisks));
                orids[instance] = nOrids++;
            }
        }

        final String[] oridDescriptions = oridDescList.toArray(new String[oridDescList.size()]);
        return new MultiFeature(name, orids, oridDescriptions);
    }

}