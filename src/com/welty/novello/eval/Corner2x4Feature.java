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

package com.welty.novello.eval;

class Corner2x4Feature extends SoloFeature {

    private static final int N_ORIDS = 6561;
    private static final String[] oridDescriptions = new String[N_ORIDS];

    static {
        for (int i = 0; i < N_ORIDS; i++) {
            oridDescriptions[i] = Base3.description(i, 8);
        }
    }

    public Corner2x4Feature() {
        super("Corner 2x4", oridDescriptions);
    }
}
