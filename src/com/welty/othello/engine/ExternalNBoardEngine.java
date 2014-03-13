package com.welty.othello.engine;

import com.welty.othello.api.NBoardEngine;
import com.welty.othello.core.ProcessLogger;
import com.welty.othello.protocol.ResponseParser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * An NBoard Engine reached via an external process.
 * <p/>
 * Responses to the gui are passed to the responseParser in a separate thread.
 */
public class ExternalNBoardEngine extends NBoardEngine {

    private final ProcessLogger processLogger;
    private volatile boolean shutdown = false;

    public ExternalNBoardEngine(@NotNull com.welty.othello.gui.ExternalEngineManager.Xei xei, boolean debug, final ResponseParser responseParser) throws IOException {
        this.processLogger = xei.createProcess(debug);

        new Thread(xei.name + " Feeder") {
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

    @Override public void sendCommand(String command) {
        processLogger.println(command);
    }
}
