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

package com.welty.othello.gui.prefs;

import java.util.prefs.Preferences;

public class PrefInt {
    private final Class<?> c;
    private final String key;
    private final int defaultValue;

    public PrefInt(Class<?> c, String key, int defaultValue) {
        this.c = c;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public int get() {
        return prefs().getInt(key, defaultValue);
    }

    private Preferences prefs() {
        return Preferences.userNodeForPackage(c);
    }

    public void put(int value) {
        prefs().putInt(key, value);
    }
}
