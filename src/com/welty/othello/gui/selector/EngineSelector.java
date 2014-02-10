package com.welty.othello.gui.selector;

import com.welty.othello.api.StatelessEngine;
import com.welty.othello.protocol.ResponseHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class EngineSelector {
    public final String name;
    public final Integer[] availableLevels;

    static final Integer[] basicLevels = {1, 2};
    public static final Integer[] advancedLevels = {1, 2, 3, 4, 5, 6, 8, 10, 12, 16, 20, 24};

    EngineSelector(String name, boolean isAdvanced) {
        this.name = name;
        this.availableLevels = isAdvanced ? advancedLevels : basicLevels;
    }

    @Override public String toString() {
        // add spaces on either side so it looks a little nicer in the JList
        return " " + name + " ";
    }

    /**
     * Starts up a new engine.
     * <p/>
     * This does not cache engines; a new engine will always be started up.
     *
     * @param maxDepth max depth for the new engine
     * @return the new engine
     * @throws IOException if one occurs while starting up an engine.
     */
    public abstract @NotNull StatelessEngine createPingEngine(int maxDepth, ResponseHandler handler) throws IOException;
}

