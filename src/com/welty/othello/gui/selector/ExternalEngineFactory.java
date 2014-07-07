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

package com.welty.othello.gui.selector;

import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.othello.api.ParsedEngine;
import com.welty.othello.api.StatelessEngine;
import com.welty.othello.gui.ExternalEngineManager;
import com.welty.othello.protocol.ResponseHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class ExternalEngineFactory extends EngineFactory {
    @NotNull private final ExternalEngineManager.Xei xei;

    public ExternalEngineFactory(@NotNull ExternalEngineManager.Xei xei) {
        super(xei.name, true, true);
        this.xei = xei;
    }

    @NotNull @Override public StatelessEngine createPingEngine(int maxDepth, ResponseHandler responseHandler) throws IOException {
        return new ParsedEngine(xei, true, responseHandler);
    }

    @Override public String strengthEstimate(int level) {
        return "External Engine";
    }

    @Override public EvalSyncEngine createEvalSyncEngine() {
        throw new IllegalStateException("not supported for external engines yet");
    }
}
