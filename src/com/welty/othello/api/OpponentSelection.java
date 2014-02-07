package com.welty.othello.api;

import com.welty.othello.gui.StatelessEngineManager;
import com.welty.othello.gui.selector.EngineSelector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OpponentSelection {
    private final EngineSelector engineSelector;
    private final int level;

    public OpponentSelection(@NotNull EngineSelector engineSelector, int level) {
        this.engineSelector = engineSelector;
        this.level = level;
    }

    /**
     * Get the engine for this selector.
     * <p/>
     * This will reuse an existing engine if one exists in the pool; otherwise it will create a new one.
     *
     * @return the Engine
     * @throws IOException
     */
    public StatelessEngine getOrCreateEngine() throws IOException {
        return StatelessEngineManager.getInstance().getOrCreate(engineSelector, level);
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
