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

    private final String machinePropsFile;

    private Props() {
        machinePropsFile = "/" + NovelloUtils.getHostName() + ".properties";
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

    public String getSourceFile() {
        return machinePropsFile;
    }
}
