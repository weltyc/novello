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

package com.welty.novello.solver;

public interface StatsListener {
    public static final StatsListener NULL = new StatsListener() {
        @Override public void update() {
        }
    };

    /**
     * Notify the listener that it's time to update the gui.
     * <p/>
     * This is called every so often during a solve, currently once per second.
     * The implementation can, for example, update node counters.
     */
    void update();
}
