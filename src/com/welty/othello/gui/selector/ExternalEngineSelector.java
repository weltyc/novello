package com.welty.othello.gui.selector;

import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.othello.api.ParsedEngine;
import com.welty.othello.api.StatelessEngine;
import com.welty.othello.protocol.ResponseHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class ExternalEngineSelector extends EngineSelector {
    private final File workingDirectory;
    private final String[] command;

    public ExternalEngineSelector(String name, String workingDirectory, String command) {
        super(name, true, true);
        this.workingDirectory = new File(workingDirectory);
        this.command = command.split("\\s+");
    }

    @NotNull @Override public StatelessEngine createPingEngine(int maxDepth, ResponseHandler responseHandler) throws IOException {
        return new ParsedEngine(name, command, workingDirectory, true, responseHandler);
    }

    @Override public String strengthEstimate(int level) {
        return "External Engine";
    }

    @Override public EvalSyncEngine createEvalSyncEngine() {
        throw new IllegalStateException("not supported for external engines yet");
    }
}
