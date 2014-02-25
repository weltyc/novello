package com.welty.othello.api;

public interface AbortCheck {

    /**
     * @return true if the search should be aborted in the middle of a round
     */
    boolean shouldAbort();

    /**
     * @return true if the search should not start an additional round of iterative deepening
     */
    boolean abortNextRound();

    /**
     * Abort check that never aborts
     */
    public static final AbortCheck NEVER = new AbortCheck() {
        @Override public boolean shouldAbort() {
            return false;
        }

        @Override public boolean abortNextRound() {
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

        @Override public boolean abortNextRound() {
            return true;
        }
    };
}
