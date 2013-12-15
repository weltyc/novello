package com.welty.novello.core;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 */
public class Props {
    private static Props instance;

    private final Properties properties;

    public static synchronized Props getInstance() {
        if (instance == null) {
            instance = new Props();
        }
        return instance;
    }

    public @Nullable String get(String key) {
        return (String) properties.get(key);
    }

    public @Nullable String get(String key, String defaultValue) {
        final String s = get(key);
        return s == null ? defaultValue : s;
    }

    private Props() {
        final String machinePropsFile = "/" + NovelloUtils.getHostName() + ".properties";
        final InputStream in = Props.class.getResourceAsStream(machinePropsFile);
        if (in == null) {
            throw new IllegalStateException("props file not found: " + machinePropsFile);
        }
        properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load props file " + machinePropsFile);
        }
    }
}
