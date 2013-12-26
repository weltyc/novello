package com.welty.novello.core;

import org.jetbrains.annotations.NotNull;

public interface Factory<T> {
    /**
     * Constructs a new T.
     * <p/>
     * The new T is guaranteed to be different from any other T generated so far.
     *
     * @return the new T.
     */
    @NotNull T construct();
}
