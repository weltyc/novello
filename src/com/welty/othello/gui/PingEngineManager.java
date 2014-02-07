package com.welty.othello.gui;

import com.welty.othello.api.PingEngine;
import com.welty.othello.gui.selector.EngineSelector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Returns external engines or creates them if they do not exist.
 */
public class PingEngineManager {
    private final Map<String, PingEngine> engines = new HashMap<>();

    private static PingEngineManager instance;

    /**
     * Get an engine from the pool; if the engine doesn't exist in the pool, create one and add to the pool.
     *
     * @param engineSelector engine to get
     * @param ping           ping id for engine switching
     * @param maxDepth       max search depth to set for the engine
     * @return the Engine
     * @throws IOException
     */
    public synchronized @NotNull PingEngine getOrCreate(@NotNull EngineSelector engineSelector, int ping, int maxDepth) throws IOException {
        PingEngine engine = engines.get(engineSelector.name);
        if (engine == null) {
            engine = engineSelector.createPingEngine(maxDepth);
            engines.put(engineSelector.name, engine);
        } else {
            engine.setMaxDepth(ping, maxDepth);
        }
        return engine;
    }

    public static synchronized @NotNull PingEngineManager getInstance() {
        if (instance == null) {
            instance = new PingEngineManager();
        }
        return instance;
    }
}
