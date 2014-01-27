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
