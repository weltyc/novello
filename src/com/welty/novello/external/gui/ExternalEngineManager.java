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

package com.welty.novello.external.gui;

import com.orbanova.common.misc.ListenerManager;
import com.orbanova.common.misc.Logger;
import com.welty.othello.core.ProcessLogger;
import com.orbanova.common.prefs.PrefSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;

public class ExternalEngineManager extends ListenerManager<ExternalEngineManager.Listener> {
    public static final ExternalEngineManager instance = new ExternalEngineManager();
    private static final Logger log = Logger.logger(ExternalEngineManager.class);

    private final PrefSet externalEngines = new PrefSet(ExternalEngineManager.class, "Engines");

    public void add(String name, String wd, String command) throws AddException {
        if (!name.matches("[a-zA-Z0-9]+")) {
            throw new AddException("Engine name must be alphanumeric (all characters must be a-z, A-Z, or 0-9)");
        }
        if (wd.contains(";")) {
            throw new AddException("Working directory cannot contain a semicolon (;)");
        }
        if (wd.isEmpty()) {
            throw new AddException("Working directory must not be empty");
        }
        if (command.isEmpty()) {
            throw new AddException("Command must not be empty");
        }
        final Path executable = Paths.get(wd).resolve(command.split("\\s+")[0]);
        if (!Files.exists(executable)) {
            throw new AddException("Executable does not exist: " + executable);
        }
        externalEngines.add(name, wd + ";" + command);
        Xei xei = new Xei(name, wd, command);
        for (Listener listener : getListeners()) {
            listener.engineAdded(xei);
        }

    }

    public List<Xei> getXei() throws BackingStoreException {
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

    private @NotNull Xei makeXei(String name, String value) {
        final String[] split = value.split(";", 2);
        final String wd = split[0];
        final String cmd = split[1];
        return new Xei(name, wd, cmd);
    }

    public void removeAll() throws BackingStoreException {
        externalEngines.removeAll();
    }

    /**
     * Get external engine info for a program.
     *
     * @return external engine info, or null if there is no info for the program.
     */
    public @Nullable Xei getXei(String program) {
        try {
            final Map<String, String> map = externalEngines.getMap();
            final String value = map.get(program);
            if (value == null) {
                return null;
            }
            return makeXei(program, value);
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String name) {
        externalEngines.delete(name);
        for (Listener listener : getListeners()) {
            listener.engineDeleted(name);
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

        @NotNull public ProcessLogger createProcess(boolean debug) throws IOException {
            final String[] command = cmd.split("\\s+");
            File wdFile = new File(wd);
            command[0] = wdFile.toPath().resolve(command[0]).toString();
            if (debug) {
                log.info("Starting external process");
                log.info("command: " + Arrays.toString(command));
                log.info("wd     : " + wdFile);
            }
            Process process = new ProcessBuilder(command).directory(wdFile).redirectErrorStream(true).start();
            return new ProcessLogger(process, debug);
        }
    }

    public interface Listener {
        /**
         * An engine was added to this Manager.
         *
         * @param xei information about the engine
         */
        void engineAdded(@NotNull Xei xei);

        /**
         * An engine was deleted from this Manager.
         *
         * @param name Engine name
         */
        void engineDeleted(@NotNull String name);
    }

    /**
     * An Exception with a user-readable error message
     */
    public static class AddException extends Exception {
        AddException(String userReadableMessage) {
            super(userReadableMessage);
        }
    }
}
