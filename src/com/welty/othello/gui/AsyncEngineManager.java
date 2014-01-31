package com.welty.othello.gui;

import com.welty.othello.gui.selector.EngineSelector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AsyncEngineManager {
    private static final Map<String, AsyncEngine> engines = new HashMap<>();

    public static synchronized @NotNull AsyncEngine getOrCreate(@NotNull EngineSelector engineSelector, int initialMaxDepth) {
        AsyncEngine engine = engines.get(engineSelector.name);
        if (engine == null) {
            engine = engineSelector.createAsyncEngine(initialMaxDepth);
            engines.put(engineSelector.name, engine);
        } else {
            engine.setMaxDepth(initialMaxDepth);
        }
        return engine;
    }
}
