package com.welty.novello.eval;

/**
*/
class CornerFeature extends Feature {
    private static final String[] oridDescriptions = {
            "No access to corner",
            "Mover access to corner",
            "Enemy access to corner",
            "Both access to corner",
            "Mover occupies corner",
            "Enemy occupies corner",
    };

    @Override public int nOrids() {
        return 6;
    }

    @Override public String oridDescription(int orid) {
        return oridDescriptions[orid];
    }
}
