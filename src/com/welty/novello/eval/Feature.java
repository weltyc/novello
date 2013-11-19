package com.welty.novello.eval;

/**
 * A Feature is a class of Terms; each Term in the class uses the Feature to get its orids.
 */
public abstract class Feature {
    int orid(int instance) {
        return instance;
    }

    /**
     * @return  Number of orids (coefficients) that this feature uses
     */
    public abstract int nOrids();

    /**
     * @return String description of the orid
     */
    public abstract String oridDescription(int orid);
}
