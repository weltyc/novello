package com.welty.novello.eval;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Vec;

/**
 * A feature that combines multiple instances into a single orid via lookup table
 */
public class MultiFeature implements Feature {
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
