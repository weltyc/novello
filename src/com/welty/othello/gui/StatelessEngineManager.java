package com.welty.othello.gui;

import com.welty.othello.api.StatelessEngine;
import com.welty.othello.gui.selector.EngineSelector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Returns engines or creates them if they do not exist.
 */
public class StatelessEngineManager {
    private final Map<String, StatelessEngine> engines = new HashMap<>();

    private static StatelessEngineManager instance;

    /**
     * Get an engine from the pool; if the engine doesn't exist in the pool, create one and add to the pool.
     *
     * @param engineSelector engine to get
     * @param maxDepth       max search depth to set for the engine
     * @return the Engine
     * @throws IOException
     */
    public synchronized @NotNull StatelessEngine getOrCreate(@NotNull EngineSelector engineSelector, int maxDepth) throws IOException {
        StatelessEngine engine = engines.get(engineSelector.name);
        if (engine == null) {
            engine = engineSelector.createPingEngine(maxDepth);
            engines.put(engineSelector.name, engine);
        }
        return engine;
    }

    public static synchronized @NotNull StatelessEngineManager getInstance() {
        if (instance == null) {
            instance = new StatelessEngineManager();
        }
        return instance;
    }
}
