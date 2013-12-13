package com.welty.novello.core;

import com.orbanova.common.feed.Mapper;
import com.orbanova.common.misc.Logger;
import org.jetbrains.annotations.NotNull;


/**
 * Displays progress of Feed handling in a ProgressUpdater.
 *
 * This is not thread-safe.
 *
 */
public class Monitor<T> implements Mapper<T, T>, AutoCloseable {
    private static final Logger log = Logger.logger(Monitor.class);

    private final ProgressUpdater progressUpdater;
    private final String message;
    long nItems;
    long nextTime;

    /**
     * Construct an object that will display the progress of a Feed on a {@link ProgressUpdater}.
     *  <p/>
     * Every 2^20 items this updates the ProgressUpdater's bar and note, and prints a log message.
     * <p/>
     * Usage:
     * <pre>
     * try (Monitor&lt;String> monitor = new Monitor&lt;String>("Loading File", numberOfLines)) {
     *     Feeds.ofLines(filename).map(monitor).handle(handler);
     * }
     * </pre>
     * @param message message to display
     * @param max     maximum number of items to be processed.
     */
    public Monitor(String message, int max) {
        this.message = message;
        this.progressUpdater = new ProgressUpdater(message, max);
        log.info("starting " + message);
    }

    @NotNull @Override public T y(T x) {
        nItems++;
        if ((nItems & 0xFFFFF) == 0) {
            final int mItems = (int) (nItems >> 20);
            progressUpdater.setProgress(mItems);
            progressUpdater.setNote(mItems + "M items loaded");
            final long t = System.currentTimeMillis();
            if (t >= nextTime) {
                log.info(message + ": " + mItems + "M items");
                nextTime = t + 5000;
            }
        }
        //noinspection SuspiciousNameCombination
        return x;
    }

    @Override public void close() {
        progressUpdater.close();
        log.info(String.format("%s ended with %,d items", message, nItems));
    }
}
