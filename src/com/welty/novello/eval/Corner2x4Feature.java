package com.welty.novello.eval;

class Corner2x4Feature extends SoloFeature {

    private static final int N_ORIDS = 6561;
    private static final String[] oridDescriptions = new String[N_ORIDS];

    static {
        for (int i = 0; i < N_ORIDS; i++) {
            oridDescriptions[i] = Base3.description(i, 8);
        }
    }

    public Corner2x4Feature() {
        super("Corner 2x4", oridDescriptions);
    }
}
