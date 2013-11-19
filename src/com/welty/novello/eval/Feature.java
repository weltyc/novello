package com.welty.novello.eval;

/**
 */
public interface Feature {
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
