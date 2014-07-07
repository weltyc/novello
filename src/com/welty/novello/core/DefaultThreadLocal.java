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

import org.jetbrains.annotations.NotNull;

/**
 * A ThreadLocal that creates an object if it doesn't exist, using the class's no-args constructor.
 */
public class DefaultThreadLocal<T> {
    private final ThreadLocal<T> threadLocal = new ThreadLocal<>();
    private final @NotNull Factory<T> factory;

    public DefaultThreadLocal(Class<T> clazz) {
        this(new DefaultFactory<>(clazz));
    }

    public DefaultThreadLocal(@NotNull Factory<T> factory) {
        this.factory = factory;
    }

    /**
     * Get the thread's object or create it if it doesn't exist.
     *
     * @return the thread's object
     */
    public @NotNull T getOrCreate() {
        T t = threadLocal.get();
        if (t == null) {
            t = factory.construct();
            threadLocal.set(t);
        }
        return t;
    }

    private static class DefaultFactory<T> implements Factory<T> {
        private final Class<T> clazz;

        private DefaultFactory(Class<T> clazz) {
            this.clazz = clazz;
        }

        @NotNull @Override public T construct() {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
