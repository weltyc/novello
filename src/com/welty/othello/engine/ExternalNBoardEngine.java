package com.welty.othello.engine;

import com.orbanova.common.misc.Logger;
import com.welty.othello.api.NBoardEngine;
import com.welty.othello.core.ProcessLogger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * An NBoard Engine reached via an external process
 */
public class ExternalNBoardEngine extends NBoardEngine {
    private static final Logger log = Logger.logger(ExternalNBoardEngine.class);

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
        if (debug) {
            log.info("Starting external process");
            log.info("command: " + Arrays.toString(command));
            log.info("wd     : " + wd);
        }
        final Process process = new ProcessBuilder(command).directory(wd).redirectErrorStream(true).start();
        return new ProcessLogger(process, debug);
    }

    @Override public void sendCommand(String command) {
        processLogger.println(command);
    }
}
