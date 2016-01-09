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

package com.welty.novello.external.gui.selector;

import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.novello.external.api.StatelessEngine;
import com.welty.othello.protocol.ResponseHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class EngineFactory {
    public final String name;
    public final Integer[] availableLevels;

    static final Integer[] basicLevels = {1, 2, 3, 4};
    public static final Integer[] advancedLevels = {1, 2, 3, 4, 5, 6, 8, 10, 12, 16, 20, 24, 26, 30, 35, 40, 50, 60};
    private final boolean external;

    /**
     *
     * @param name Engine name
     * @param isAdvanced true if the engine selection box should display advanced levels for this engine
     * @param isExternal true if the engine is an external engine
     */
    EngineFactory(String name, boolean isAdvanced, boolean isExternal) {
        this.name = name;
        this.external = isExternal;
        this.availableLevels = isAdvanced ? advancedLevels : basicLevels;
    }

    @Override public String toString() {
        // add spaces on either side so it looks a little nicer in the JList
        return " " + name + " ";
    }

    /**
     * Starts up a new engine.
     * <p/>
     * This does not cache engines; a new engine will always be started up.
     *
     * @param maxDepth max depth for the new engine
     * @return the new engine
     * @throws IOException if one occurs while starting up an engine.
     */
    public abstract @NotNull StatelessEngine createPingEngine(int maxDepth, ResponseHandler handler) throws IOException;

    /**
     * Get a String containing a textual representation of the opponent's strength.
     *
     * The representation may include HTML tags if the representation starts with &lt;html> and ends with
     * &lt;/html>
     */
    public abstract String strengthEstimate(int level);

    public abstract EvalSyncEngine createEvalSyncEngine();

    /**
     * @return true if this is an external engine
     */
    public boolean isExternal() {
        return external;
    }

    public String getName() {
        return name;
    }
}

