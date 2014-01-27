package com.welty.othello.gui.prefs;

import java.util.prefs.Preferences;

public class PrefString {
    private final Class<?> c;
    private final String key;
    private final String defaultValue;

    public PrefString(Class<?> c, String key, String defaultValue) {
        this.c = c;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String get() {
        return prefs().get(key, defaultValue);
    }

    private Preferences prefs() {
        return Preferences.userNodeForPackage(c);
    }

    public void put(String value) {
        prefs().put(key, value);
    }
}
