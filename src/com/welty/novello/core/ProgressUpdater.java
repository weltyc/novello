package com.welty.novello.core;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private final int max;
    private int progress;
    private String note = "";
    private boolean closed = false;
    private String autoNote = null;

    /**
     * Constructs a thread-safe {@link ProgressMonitor}
     *
     * @param message name of operation being performed
     * @param max     maximum value of progress. When this is reached, the ProgressMonitor will disappear.
     */
    public ProgressUpdater(final String message, final int max) {
        this.max = max;
        // we need to create the Progress Monitor on the Event Dispatch Thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                new ProgressThread(message, max);
            }
        });
    }

    /**
     * Add 1 to the progress number, in a thread-safe manner.
     */
    public synchronized void update() {
        progress++;
    }

    /**
     * Update the ProgressMonitor. Does not set a note.
     *
     * @param nComplete current value of the progress bar
     */
    public synchronized void setProgress(int nComplete) {
        this.progress = nComplete;
    }

    /**
     * Closes the ProgressMonitor.
     */
    public synchronized void close() {
        closed = true;
    }

    /**
     * Sets the note on the progress monitor.
     *
     * @param note note to set
     */
    public synchronized void setNote(String note) {
        this.note = note;
    }

    /**
     * If autoNote is set, the note on the progress bar is automatically set to something like
     * "13,428k of 21,477k {itemName}"
     *
     * @param suffix item description
     */
    public synchronized void setAutoNote(String suffix) {
        autoNote = suffix;
    }

    public synchronized int getProgress() {
        return progress;
    }


    private synchronized String getNote() {
        if (autoNote != null) {
            note = NovelloUtils.engineeringLong(progress) + " / " + NovelloUtils.engineeringLong(max) + " " + autoNote;
        }
        return note;
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    /**
     * The internal guts, containing stuff accessed on the Event Dispatch Thread
     */
    private class ProgressThread implements ActionListener {
        private final ProgressMonitor progressMonitor;
        private final Timer timer;

        ProgressThread(String message, int max) {
            progressMonitor = new ProgressMonitor(null, message, "", 0, max);

            timer = new Timer(250, this);
            timer.setDelay(250);
            timer.start();
        }

        @Override public void actionPerformed(ActionEvent e) {
            try {
                // This is called from the EDT.
                progressMonitor.setProgress(getProgress());
                progressMonitor.setNote(getNote());
                if (isClosed()) {
                    timer.stop();
                    progressMonitor.close();
                }
            } catch (NullPointerException ex) {
                // mac running with screen saver on
                // ignore.
            }
        }
    }
}
