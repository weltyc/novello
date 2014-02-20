package com.welty.othello.api;

public interface AbortCheck {

    /**
     * @return true if the search should be aborted
     */
    boolean shouldAbort();

    /**
     * Abort check that never aborts
     */
    public static final AbortCheck NEVER = new AbortCheck() {
        @Override public boolean shouldAbort() {
            return false;
        }
    };

    /**
     * Abort check that always aborts
     */
    public static final AbortCheck ALWAYS = new AbortCheck() {
        @Override public boolean shouldAbort() {
            return true;
        }
    };
}
