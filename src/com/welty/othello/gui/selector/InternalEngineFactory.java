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

package com.welty.othello.gui.selector;

import com.orbanova.common.feed.Mapper;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.othello.api.StatelessEngine;
import com.welty.othello.api.SyncStatelessEngine;
import com.welty.othello.protocol.ResponseHandler;
import org.jetbrains.annotations.NotNull;

public class InternalEngineFactory extends EngineFactory {
    private final @NotNull Eval eval;
    private final @NotNull String options;
    private final Mapper<Integer, String> strengthEstimator;

    /**
     * @param name       name of player in opponent selection window
     * @param isAdvanced if true, offer levels > 4 in opponent selection window
     * @param options    MidgameSearch.Options
     * @param eval       eval code, e.g. "d2"
     */
    private InternalEngineFactory(@NotNull String name, boolean isAdvanced, @NotNull String options, @NotNull Eval eval
            , @NotNull Mapper<Integer, String> strengthEstimator) {
        super(name, isAdvanced, false);
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

    static @NotNull InternalEngineFactory of(@NotNull String name, boolean isAdvanced, @NotNull String options
            , @NotNull Eval eval, final @NotNull String strength) {
        final Mapper<Integer, String> mapper = new Mapper<Integer, String>() {
            @NotNull @Override public String y(Integer x) {
                return strength;
            }
        };
        return of(name, isAdvanced, options, eval, mapper);
    }

    static @NotNull InternalEngineFactory of(String name, boolean advanced, String options, Eval eval, Mapper<Integer, String> mapper) {
        return new InternalEngineFactory(name, advanced, options, eval, mapper);
    }
}
