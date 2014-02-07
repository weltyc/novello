package com.welty.othello.gui.selector;

import com.welty.novello.selfplay.Players;
import com.welty.othello.api.PingEngine;
import com.welty.othello.api.SyncPingEngine;
import org.jetbrains.annotations.NotNull;

public class InternalEngineSelector extends EngineSelector {
    private final String eval;
    private final String options;

    /**
     * Simple eval selector
     *
     * @param name eval name, as sent to Players.eval().
     */
    public InternalEngineSelector(String name) {
        this(name, false, name, "NS");
    }

    /**
     * @param name       name of player in opponent selection window
     * @param isAdvanced if true, offer levels > 2 in opponent selection window
     * @param eval       eval code, e.g. "d2"
     * @param options    MidgameSearch.Options
     */
    public InternalEngineSelector(String name, boolean isAdvanced, String eval, String options) {
        super(name, isAdvanced);
        this.eval = eval;
        this.options = options;
    }

    @Override public @NotNull PingEngine createPingEngine(int initialMaxDepth) {
        return new SyncPingEngine(Players.eval(eval), initialMaxDepth, options);
    }
}
