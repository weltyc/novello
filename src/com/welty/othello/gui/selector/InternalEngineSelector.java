package com.welty.othello.gui.selector;

import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.novello.selfplay.Players;
import com.welty.othello.gui.AsyncEngine;
import com.welty.othello.gui.AsyncEngineAdapter;

class InternalEngineSelector extends EngineSelector {
    private final String eval;
    private final String options;

    /**
     * Simple eval selector
     *
     * @param name eval name, as sent to Players.eval().
     */
    InternalEngineSelector(String name) {
        this(name, false, name, "NS");
    }

    /**
     * @param name       name of player in opponent selection window
     * @param isAdvanced if true, offer levels > 2 in opponent selection window
     * @param eval       eval code, e.g. "d2"
     * @param options    MidgameSearch.Options
     */
    InternalEngineSelector(String name, boolean isAdvanced, String eval, String options) {
        super(name, isAdvanced);
        this.eval = eval;
        this.options = options;
    }

    @Override public AsyncEngine createAsyncEngine(int initialMaxDepth) {
        return new AsyncEngineAdapter(new EvalSyncEngine(Players.eval(eval), initialMaxDepth, options));
    }
}
