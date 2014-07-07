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


import com.orbanova.common.misc.ListenerManager;
import org.jetbrains.annotations.NotNull;

public abstract class OpponentSelector extends ListenerManager<OpponentSelector.Listener> {
    /**
     * Get the currently selected Opponent.
     *
     * @return the Opponent
     */
    public abstract @NotNull OpponentSelection getOpponent();

    public interface Listener {
        /**
         * Notify the caller that the opponent has been changed.
         * It can get the new opponent via the get() method of this object
         */
        public void opponentChanged();
    }
}
