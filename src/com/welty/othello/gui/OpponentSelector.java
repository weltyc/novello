package com.welty.othello.gui;


import com.orbanova.common.misc.ListenerManager;
import org.jetbrains.annotations.NotNull;

public abstract class OpponentSelector extends ListenerManager<OpponentSelector.Listener> {
    /**
     * Get the currently selected Opponent.
     *
     * @return the Opponent
     */
    public abstract @NotNull OpponentSelection getOpponent();

    public interface Listener {
        /**
         * Notify the caller that the opponent has been changed.
         * It can get the new opponent via the get() method of this object
         */
        public void opponentChanged();
    }
}
