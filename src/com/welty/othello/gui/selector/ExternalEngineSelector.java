package com.welty.othello.gui.selector;

import com.welty.novello.ntest.NBoardSyncEngine;
import com.welty.othello.gui.AsyncEngine;
import com.welty.othello.gui.AsyncEngineAdapter;

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
