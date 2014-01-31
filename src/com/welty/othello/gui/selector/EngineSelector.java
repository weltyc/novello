package com.welty.othello.gui.selector;

import com.welty.othello.gui.AsyncEngine;

public abstract class EngineSelector {
    public final String name;
    final Integer[] availableLevels;

    static final Integer[] basicLevels = {1, 2};
    static final Integer[] advancedLevels = {1, 2, 3, 4, 5, 6, 8, 10, 12, 16, 20, 24};

    EngineSelector(String name, boolean isAdvanced) {
        this.name = name;
        this.availableLevels = isAdvanced ? advancedLevels : basicLevels;
    }

    @Override public String toString() {
        // add spaces on either side so it looks a little nicer in the JList
        return " " + name + " ";
    }

    public abstract AsyncEngine createAsyncEngine(int initialMaxDepth);
}

