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

package com.welty.novello.coca;

import com.welty.novello.core.MeValue;

import java.io.IOException;
import java.util.List;

/**
 * Produces a list of MeValues suitable for coefficient calculation
 */
public interface MvSource {
    /**
     * Get a list of MeValues suitable for coefficient calculation
     *
     * @return the list of MeValues
     */
    public List<MeValue> getMvs() throws IOException;
}
