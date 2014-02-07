package com.welty.othello.engine;

import com.welty.othello.api.NBoardEngine;
import com.welty.othello.core.ProcessLogger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * An NBoard Engine reached via an external process
 */
public class ExternalNBoardEngine extends NBoardEngine {
    private final ProcessLogger processLogger;
    private volatile boolean shutdown = false;

    public ExternalNBoardEngine() throws IOException {
        this("./mEdax -nboard".split("\\s+"), new File("/Applications/edax/4.4/bin"), true);
    }

    public ExternalNBoardEngine(String[] command, File wd, boolean debug) throws IOException {
        this.processLogger = createProcessLogger(command, wd, debug);

        new Thread("NBoard Feeder") {
            @Override public void run() {
                while (!shutdown) {
                    try {
                        final String line = processLogger.readLine();

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                fireMessageReceived(line);
                            }
                        });
                    } catch (IOException e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                fireEngineTerminated();
                            }
                        });
                    }
                }
            }
        }.start();
    }

    private static ProcessLogger createProcessLogger(String[] command, File wd, boolean debug) throws IOException {
        final Process process = new ProcessBuilder(command).directory(wd).redirectErrorStream(true).start();
        return new ProcessLogger(process, debug);
    }

    @Override public void sendCommand(String command) {
        processLogger.println(command);
    }
}
