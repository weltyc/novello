package com.welty.novello.solver;

public interface StatsListener {
    public static final StatsListener NULL = new StatsListener() {
        @Override public void update() {
        }
    };

    /**
     * Notify the listener that it's time to update the gui.
     * <p/>
     * This is called every so often during a solve, currently once per second.
     * The implementation can, for example, update node counters.
     */
    void update();
}
