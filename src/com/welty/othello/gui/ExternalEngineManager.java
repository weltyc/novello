package com.welty.othello.gui;

import com.welty.othello.gui.prefs.PrefSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;

public class ExternalEngineManager {
    public static final PrefSet externalEngines = new PrefSet(ExternalEngineManager.class, "Engines");

    public static void add(String name, String wd, String command) {
        externalEngines.add(name, wd + ";" + command);
    }

    public static List<Xei> getXei() throws BackingStoreException {
        final Map<String, String> map = externalEngines.getMap();
        List<Xei> xeis = new ArrayList<>();

        for (Map.Entry<String, String> kv : map.entrySet()) {
            final String name = kv.getKey();
            final String value = kv.getValue();
            final Xei xei = makeXei(name, value);
            xeis.add(xei);
        }
        return xeis;
    }

    private static Xei makeXei(String name, String value) {
        final String[] split = value.split(";", 2);
        final String wd = split[0];
        final String cmd = split[1];
        return new Xei(name, wd, cmd);
    }

    public static void removeAll() throws BackingStoreException {
        externalEngines.removeAll();
    }

    /**
     * Get external engine info for a program.
     *
     * @return external engine info, or null if there is no info for the program.
     */
    public static Xei getXei(String program) {
        try {
            final String value = externalEngines.getMap().get(program);
            if (value == null) {
                return null;
            }
            return makeXei(program, value);
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Xei {
        public final String name;
        public final String wd;
        public final String cmd;

        public Xei(String name, String wd, String cmd) {
            this.name = name;
            this.wd = wd;
            this.cmd = cmd;
        }
    }
}
