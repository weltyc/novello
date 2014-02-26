package com.welty.othello.gui.selector;

import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.Players;
import com.welty.othello.api.StatelessEngine;
import com.welty.othello.api.SyncStatelessEngine;
import com.welty.othello.protocol.ResponseHandler;
import org.jetbrains.annotations.NotNull;

public class InternalEngineSelector extends EngineSelector {
    private final @NotNull Eval eval;
    private final @NotNull String options;

    /**
     * Simple eval selector
     *
     * @param name eval name, as sent to Players.eval().
     */
    public InternalEngineSelector(@NotNull String name) {
        this(name, false, "NS", Players.eval(name));
    }

    /**
     * @param name       name of player in opponent selection window
     * @param isAdvanced if true, offer levels > 2 in opponent selection window
     * @param options    MidgameSearch.Options
     * @param eval       eval code, e.g. "d2"
     */
    public InternalEngineSelector(@NotNull String name, boolean isAdvanced, @NotNull String options, @NotNull Eval eval) {
        super(name, isAdvanced);
        this.eval = eval;
        this.options = options;
    }

    @Override public @NotNull StatelessEngine createPingEngine(int initialMaxDepth, ResponseHandler responseHandler) {
        return new SyncStatelessEngine(name, eval, options, responseHandler);
    }

    static @NotNull InternalEngineSelector of(@NotNull String name, boolean isAdvanced, @NotNull String options, @NotNull Eval eval) {
        return new InternalEngineSelector(name, isAdvanced, options, eval);
    }
}
