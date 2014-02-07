package com.welty.othello.gui.selector;

import com.welty.othello.api.ParsedEngine;
import com.welty.othello.api.StatelessEngine;
import com.welty.othello.engine.ExternalNBoardEngine;
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

    @NotNull @Override public StatelessEngine createPingEngine(int maxDepth) throws IOException {
        final ExternalNBoardEngine ext = new ExternalNBoardEngine(command, workingDirectory, true);
        return new ParsedEngine(ext);
    }
}
