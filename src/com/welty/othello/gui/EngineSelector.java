package com.welty.othello.gui;

import com.welty.novello.ntest.NBoardSyncEngine;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.novello.selfplay.Players;

abstract class EngineSelector {
    final String name;
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

class ExternalEngineSelector extends EngineSelector {
    private final String workingDirectory;
    private final String command;

    ExternalEngineSelector(String name, String workingDirectory, String command) {
        super(name, true);
        this.workingDirectory = workingDirectory;
        this.command = command;
    }

    @Override public AsyncEngine createAsyncEngine(int initialMaxDepth) {
        return new AsyncEngineAdapter(new NBoardSyncEngine(this.name, initialMaxDepth, false, workingDirectory, command));
    }
}

class InternalEngineSelector extends EngineSelector {
    private final String eval;
    private final String options;

    /**
     * Simple eval selector
     *
     * @param name eval name, as sent to Players.eval().
     */
    InternalEngineSelector(String name) {
        this(name, false, name, "NS");
    }

    /**
     * @param name       name of player in opponent selection window
     * @param isAdvanced if true, offer levels > 2 in opponent selection window
     * @param eval       eval code, e.g. "d2"
     * @param options    MidgameSearch.Options
     */
    InternalEngineSelector(String name, boolean isAdvanced, String eval, String options) {
        super(name, isAdvanced);
        this.eval = eval;
        this.options = options;
    }

    @Override public AsyncEngine createAsyncEngine(int initialMaxDepth) {
        return new AsyncEngineAdapter(new EvalSyncEngine(Players.eval(eval), initialMaxDepth, options));
    }
}
