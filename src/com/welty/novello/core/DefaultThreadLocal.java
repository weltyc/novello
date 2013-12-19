package com.welty.novello.core;

import org.jetbrains.annotations.NotNull;

/**
 * A ThreadLocal that creates an object if it doesn't exist, using the class's no-args constructor.
 */
public class DefaultThreadLocal<T> {
    private final ThreadLocal<T> threadLocal = new ThreadLocal<>();
    private final Class<T> clazz;

    public DefaultThreadLocal(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Get the thread's object or create it if it doesn't exist.
     *
     * @return the thread's object
     */
    public @NotNull T getOrCreate() {
        T t = threadLocal.get();
        if (t == null) {
            try {
                t = clazz.newInstance();
                threadLocal.set(t);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return t;
    }
}
