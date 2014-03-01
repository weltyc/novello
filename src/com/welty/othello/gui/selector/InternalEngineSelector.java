package com.welty.othello.gui.selector;

import com.orbanova.common.feed.Mapper;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.othello.api.StatelessEngine;
import com.welty.othello.api.SyncStatelessEngine;
import com.welty.othello.protocol.ResponseHandler;
import org.jetbrains.annotations.NotNull;

public class InternalEngineSelector extends EngineSelector {
    private final @NotNull Eval eval;
    private final @NotNull String options;
    private final Mapper<Integer, String> strengthEstimator;

    /**
     * @param name       name of player in opponent selection window
     * @param isAdvanced if true, offer levels > 4 in opponent selection window
     * @param options    MidgameSearch.Options
     * @param eval       eval code, e.g. "d2"
     */
    private InternalEngineSelector(@NotNull String name, boolean isAdvanced, @NotNull String options, @NotNull Eval eval
    , @NotNull Mapper<Integer, String> strengthEstimator) {
        super(name, isAdvanced);
        this.eval = eval;
        this.options = options;
        this.strengthEstimator = strengthEstimator;
    }

    @Override public @NotNull StatelessEngine createPingEngine(int initialMaxDepth, ResponseHandler responseHandler) {
        return new SyncStatelessEngine(name, eval, options, responseHandler);
    }

    @Override public String strengthEstimate(int level) {
        return strengthEstimator.y(level);
    }

    @Override public EvalSyncEngine createEvalSyncEngine() {
        return new EvalSyncEngine(eval, options, name);
    }

    static @NotNull InternalEngineSelector of(@NotNull String name, boolean isAdvanced, @NotNull String options
            , @NotNull Eval eval, final @NotNull String strength) {
        final Mapper<Integer, String> mapper = new Mapper<Integer, String>() {
            @NotNull @Override public String y(Integer x) {
                return strength;
            }
        };
        return of(name, isAdvanced, options, eval, mapper);
    }

    static @NotNull InternalEngineSelector of(String name, boolean advanced, String options, Eval eval, Mapper<Integer, String> mapper) {
        return new InternalEngineSelector(name, advanced, options, eval, mapper);
    }
}
