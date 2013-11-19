package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;

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
