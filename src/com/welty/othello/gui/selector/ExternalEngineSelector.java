package com.welty.othello.gui.selector;

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
        super(name, true);
        this.workingDirectory = new File(workingDirectory);
        this.command = command.split("\\s+");
    }

    @NotNull @Override public StatelessEngine createPingEngine(int maxDepth, ResponseHandler responseHandler) throws IOException {
        return new ParsedEngine(command, workingDirectory, true, responseHandler);
    }
}
