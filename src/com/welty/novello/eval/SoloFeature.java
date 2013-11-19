package com.welty.novello.eval;

/**
 * A Feature that has a 1-to-1 mapping between instances and orids.
 */
public class SoloFeature implements Feature {
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
