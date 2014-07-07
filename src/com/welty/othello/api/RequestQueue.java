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

package com.welty.othello.api;

import com.orbanova.common.misc.Require;
import org.jetbrains.annotations.NotNull;

/**
 * A queue with zero or one element.
 * <p/>
 * If the request queue is empty, add() adds the request to the queue and take() blocks until add() is called.
 * If the request queue is nonempty, add() replaces the request currently in the queue and take() removes it.
 */
public final class RequestQueue {
    private Runnable request;

    /**
     * Add a request to the queue.
     *
     * @param request the request
     */
    public synchronized void add(@NotNull Runnable request) {
        Require.notNull(request);
        this.request = request;
        notify();
    }

    /**
     * Get a request from the queue, blocking if necessary until one becomes available.
     *
     * @return the request
     * @throws InterruptedException
     */
    public synchronized Runnable take() throws InterruptedException {
        while (request == null) {
            wait();
        }
        final Runnable result = request;
        request = null;
        return result;
    }

    /**
     * @return true if a request has been added to the queue
     */
    public synchronized boolean hasRequest() {
        return request != null;
    }
}
