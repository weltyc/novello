package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;

import java.util.ArrayList;
import java.util.List;

/**
 */
interface Feature {
    /**
     * @return  Number of orids (distinct instances) for this feature
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
    static final SoloFeature cornerFeature = new SoloFeature(
            "No access to corner",
            "Mover access to corner",
            "Enemy access to corner",
            "Both access to corner",
            "Mover occupies corner",
            "Enemy occupies corner"
    );
}


/**
 * A feature that combines multiple instances into a single orid via lookup table
 */
class MultiFeature implements Feature {
    private final int[] orids;
    private final String[] oridDescriptions;

    public MultiFeature(int[] orids, String[] oridDescriptions) {
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
}

/**
 * A Feature that has a 1-to-1 mapping between instances and orids.
 */
class SoloFeature implements Feature {
    private final String[] oridDescriptions;

    public SoloFeature(String... oridDescriptions) {
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
}

/**
 * A Feature that uses, as its instance, a base-3 representation of the disks in a line.
 *
 * 0 = empty, 1=mover, 2=enemy.
 * Any disk pattern gives the same orid as reversing its disks.
 *
 * Disk patterns are displayed assuming black is the mover; * = mover, O = enemy.
 */
class LinePatternFeatureFactory {
    static Feature of(int nDisks)  {
        final int[] orids = new int[nInstances(nDisks)];
        final List<String> oridDescList = new ArrayList<>();

        int nOrids=0;
        for (int instance = 0; instance < orids.length; instance++) {
            final int reverse = reverse(instance, nDisks);
            if (reverse<instance) {
                orids[instance] = orids[reverse];
            }
            else {
                oridDescList.add(oridDescription(instance, nDisks));
                orids[instance] = nOrids++;
            }
        }

        final String[] oridDescriptions = oridDescList.toArray(new String[oridDescList.size()]);
        return new MultiFeature(orids, oridDescriptions);
    }

    /**
     * Reverse the base 3 digits of instance
     *
     * @param instance initial input
     * @return reversed instance
     */
    static int reverse(int instance, int digits) {
        int reverse = 0;
        for (int d=0; d<digits; d++) {
            reverse*=3;
            reverse += instance%3;
            instance /=3;
        }
        return reverse;
    }

    private static final char[] output = ".*O".toCharArray();

    /**
     * Generate oridDescription from one of the orid's instances
     *
     * @param instance instance. NOT orid.
     * @param nDisks number of disks.
     * @return oridDescription
     */
    static String oridDescription(int instance, int nDisks) {
        final StringBuilder sb = new StringBuilder();
        while (instance>0) {
            sb.append(output[instance%3]);
            instance /= 3;
        }
        while (sb.length()<nDisks) {
            sb.append(output[0]);
        }
        return sb.reverse().toString();
    }

    static int nInstances(int nDisks) {
        int nInstances = 1;
        for (int i=0; i<nDisks; i++) {
            nInstances*=3;
        }
        return nInstances;
    }
}