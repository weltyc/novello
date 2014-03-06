package com.welty.othello.gui.prefs;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A map String->String stored using Preferences.
 */
public class PrefSet {
    private final Class<?> c;
    private final String key;

    /**
     * @param c   class. This class's package is used as a location to store preferences.
     * @param subnodeName name of subnode under which data will be stored
     */
    public PrefSet(Class<?> c, String subnodeName) {
        this.c = c;
        this.key = subnodeName;
    }

    public Map<String, String> getMap() throws BackingStoreException {
        final Preferences node = prefs();
        final String[] keys = node.keys();
        final HashMap<String, String> result = new HashMap<>();
        for (String key : keys) {
            result.put(key, node.get(key, null));
        }
        return result;
    }

    private Preferences prefs() {
        return Preferences.userNodeForPackage(c).node(key);
    }

    public void add(String key, String value) {
        final Preferences prefs = prefs();
        prefs.put(key, value);
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeAll() throws BackingStoreException {
        prefs().removeNode();
    }

    public void delete(String name) {
        final Preferences prefs = prefs();
        prefs.remove(name);
    }
}
