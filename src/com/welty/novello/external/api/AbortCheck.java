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

package com.welty.novello.external.api;

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
