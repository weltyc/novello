package com.welty.novello.eval;

/**
 * A Feature that has a 1-to-1 mapping between instances and orids.
 */
public abstract class SoloFeature implements Feature {
    @Override public int orid(int instance) {
        return instance;
    }

    @Override public int nInstances() {
        return nOrids();
    }
}
