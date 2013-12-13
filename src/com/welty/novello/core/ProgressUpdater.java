package com.welty.novello.core;

import javax.swing.*;

/**
 * Thread-safe wrapper for ProgressMonitor
 * <p/>
 * This fixes some problems with the ProgressMonitor: <br/>
 * 1. It's not clear if ProgressMonitor is thread-safe. This is. <br/>
 * 2. ProgressMonitor.setProgress() can throw NullPointerException deep in its internals (seen on a Mac with screen-saver
 * on). This ignores the NPE. <br/>
 * 3. ProgressUpdater implements AutoCloseable so it can be used in try-with-resources. <br/>
 * 4. ProgressMonitor has no way for multi-threaded tasks to update progress. ProgressUpdater has an update() method
 * which increments an internal counter by 1.
 */
public class ProgressUpdater implements AutoCloseable {
    private int nComplete;
    private final ProgressMonitor progressMonitor;

    /**
     * Constructs a thread-safe {@link ProgressMonitor}
     *
     * @param message name of operation being performed
     * @param max     maximum value of progress. When this is reached, the ProgressMonitor will disappear.
     */
    public ProgressUpdater(String message, int max) {
        progressMonitor = new ProgressMonitor(null, message, "", 0, max);
    }

    /**
     * Add 1 to the progress number, in a thread-safe manner. Also updates the text.
     */
    public synchronized void update() {
        nComplete++;
        progressMonitor.setProgress(nComplete);
        progressMonitor.setNote(String.format("%,d / %,d", nComplete, progressMonitor.getMaximum()));
    }

    /**
     * Update the ProgressMonitor. Does not set a note.
     *
     * @param nComplete current value of the progress bar
     */
    public synchronized void setProgress(int nComplete) {
        this.nComplete = nComplete;
        progressMonitor.setProgress(nComplete);
    }

    public synchronized void close() {
        progressMonitor.close();
    }

    /**
     * Sets the note on the progress monitor.
     *
     * @param note note to set
     */
    public synchronized void setNote(String note) {
        progressMonitor.setNote(note);
    }
}
