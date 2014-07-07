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

package com.welty.othello.api;

import com.welty.othello.gui.StatelessEngineManager;
import com.welty.othello.gui.selector.EngineFactory;
import com.welty.othello.protocol.ResponseHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class OpponentSelection {
    private final @NotNull EngineFactory engineFactory;
    private final int level;

    public OpponentSelection(@NotNull EngineFactory engineFactory, int level) {
        this.engineFactory = engineFactory;
        this.level = level;
    }

    /**
     * Get the engine for this selector.
     * <p/>
     * This will reuse an existing engine if one exists in the pool; otherwise it will create a new one.
     *
     * @param responseHandler handler for engine responses
     * @return the Engine
     * @throws IOException
     */
    public StatelessEngine getOrCreateEngine(ResponseHandler responseHandler) throws IOException {
        return StatelessEngineManager.getInstance().getOrCreate(engineFactory, level, responseHandler);
    }

    /**
     * Meant for use by the OpponentManager, mostly.
     * <p/>
     * I may reduce the visibility on this method.
     *
     * @return opponent level
     */
    public int getMaxDepth() {
        return this.level;
    }

    @Override public String toString() {
        return engineFactory.name;
    }

    /**
     * Get a String containing a textual representation of the opponent's strength.
     *
     * The representation may include HTML tags if the representation starts with &lt;html> and ends with
     * &lt;/html>
     */
    public String strengthEstimate() {
        return engineFactory.strengthEstimate(level);
    }
}
