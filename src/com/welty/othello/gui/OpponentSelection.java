package com.welty.othello.gui;

import com.welty.othello.gui.selector.EngineSelector;
import org.jetbrains.annotations.NotNull;

public class OpponentSelection {
    private final EngineSelector engineSelector;
    private final int level;

    public OpponentSelection(@NotNull EngineSelector engineSelector, int level) {
        this.engineSelector = engineSelector;
        this.level = level;
    }

    public AsyncEngine getAsyncEngine() {
        return AsyncEngineManager.getOrCreate(engineSelector, level);
    }

    /**
     * Meant for use by the OpponentManager, mostly.
     * <p/>
     * I may reduce the visibility on this method.
     *
     * @return opponent level
     */
    public int getMaxDepth() {
        return this.level;
    }
}
