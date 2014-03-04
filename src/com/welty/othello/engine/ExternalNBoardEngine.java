package com.welty.othello.engine;

import com.orbanova.common.misc.Logger;
import com.welty.othello.api.NBoardEngine;
import com.welty.othello.core.ProcessLogger;
import com.welty.othello.protocol.ResponseParser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * An NBoard Engine reached via an external process.
 * <p/>
 * Responses to the gui are passed to the responseParser in a separate thread.
 */
public class ExternalNBoardEngine extends NBoardEngine {
    private static final Logger log = Logger.logger(ExternalNBoardEngine.class);

    private final ProcessLogger processLogger;
    private volatile boolean shutdown = false;

    public ExternalNBoardEngine(String[] command, File wd, boolean debug, final ResponseParser responseParser) throws IOException {
        this.processLogger = createProcessLogger(command, wd, debug);

        new Thread(command[0] + " Feeder") {
            @Override public void run() {
                while (!shutdown) {
                    try {
                        final String msg = processLogger.readLine();
                        responseParser.handle(msg);
                    } catch (IOException e) {
                        responseParser.engineTerminated();
                    }
                }
            }
        }.start();
    }

    private static ProcessLogger createProcessLogger(String[] command, File wd, boolean debug) throws IOException {
        command[0] = wd.toPath().resolve(command[0]).toString();
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
