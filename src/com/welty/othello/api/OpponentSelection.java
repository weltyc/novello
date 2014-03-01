package com.welty.othello.api;

import com.welty.othello.gui.StatelessEngineManager;
import com.welty.othello.gui.selector.EngineSelector;
import com.welty.othello.protocol.ResponseHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OpponentSelection {
    private final @NotNull EngineSelector engineSelector;
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
     * @param responseHandler handler for engine responses
     * @return the Engine
     * @throws IOException
     */
    public StatelessEngine getOrCreateEngine(ResponseHandler responseHandler) throws IOException {
        return StatelessEngineManager.getInstance().getOrCreate(engineSelector, level, responseHandler);
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

    @Override public String toString() {
        return engineSelector.name;
    }

    /**
     * Get a String containing a textual representation of the opponent's strength.
     *
     * The representation may include HTML tags if the representation starts with &lt;html> and ends with
     * &lt;/html>
     */
    public String strengthEstimate() {
        return engineSelector.strengthEstimate(level);
    }
}
