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

import org.jetbrains.annotations.NotNull;

/**
 * A MoveSorter for each depth
 * <p/>
 * Having a separate MoveSorter for each depth lets us sort moves while doing another search to score moves
 * for a deeper search.
 */
class MoveSorters {
    private final MoveSorter[] sorters = new MoveSorter[64];

    MoveSorters(@NotNull Counter counter, @NotNull MidgameSearcher midgameSearcher) {
        for (int i = 0; i < sorters.length; i++) {
            sorters[i] = new MoveSorter(counter, midgameSearcher);
        }
    }

    public MoveSorter get(int nEmpties) {
        return sorters[nEmpties];
    }
}
