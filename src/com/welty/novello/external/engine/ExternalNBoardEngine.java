/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.external.engine;

import com.welty.novello.external.api.NBoardEngine;
import com.welty.novello.external.gui.ExternalEngineManager;
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

    public ExternalNBoardEngine(@NotNull ExternalEngineManager.Xei xei, boolean debug, final ResponseParser responseParser) throws IOException {
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
