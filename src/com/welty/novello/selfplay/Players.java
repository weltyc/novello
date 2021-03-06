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

package com.welty.novello.selfplay;

import com.welty.novello.eval.*;
import com.welty.novello.ntest.NBoardSyncEngine;
import com.welty.ntestj.CEvaluatorJ;
import com.welty.novello.external.gui.ExternalEngineManager;
import com.welty.novello.external.gui.selector.EngineFactory;
import com.welty.novello.external.gui.selector.InternalEngineFactoryManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class containing Othello players
 */
public class Players {
    public static Eval eval(String name) {
        switch (name) {
            case "ntestJ":
                return CEvaluatorJ.getInstance();
            case "ntestK":
                return EvalStrategyJ.getNtestEval();
            default:
                EvalStrategy strategy = EvalStrategies.strategy(name.substring(0, 1));
                return new CoefficientEval(strategy, name.substring(1));
        }
    }

    /**
     * Constructs a player with debug=false.
     * <p/>
     * See {@link #player(String, boolean)}
     */
    public static SyncPlayer player(String textString) {
        return player(textString, false);
    }

    /**
     * Construct a SyncPlayer from a text string
     * <p/>
     * The text string has the format
     * {eval}:{depth}{options}
     * <p/>
     * {eval} is an evaluation strategy followed by a coefficient set, for example "d2"
     * <p/>
     * {depth} is an integer.
     * <p/>
     * {options} is a list of characters interpreted as option flags; see {@link com.welty.novello.solver.MidgameSearcher.Options}
     * for a list available options.
     * <p/>
     * {eval} may also be "ntest", in which case an external NTest process is launched. NTest ignores options.
     * <p/>
     * This function always returns a new player; it does not reuse old players even if the textString is the same.
     * This is because the SyncEngine may not be multithreaded.
     *
     * @param textString player text string
     * @param debug      if true, constructs external engines with debug information.
     * @return a newly constructed SyncEngine.
     */
    public static SyncPlayer player(String textString, boolean debug) {
        final String[] parts = textString.split(":", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("require an eval and a search depth, for instance 'a1:3w'; had " + textString);
        }

        final Pattern pattern = Pattern.compile("([0-9]+)([a-zA-Z]*)");
        final Matcher matcher = pattern.matcher(parts[1]);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal depth and options: " + parts[1]);
        }

        final int depth = Integer.parseInt(matcher.group(1));

        final String evalName = parts[0];
        final ExternalEngineManager.Xei xei = ExternalEngineManager.instance.getXei(evalName);
        if (xei != null) {
            return new SyncPlayer(new NBoardSyncEngine(xei, debug), depth);
        } else {
            final EngineFactory engineFactory = InternalEngineFactoryManager.instance.get(evalName);
            final EvalSyncEngine engine;
            if (engineFactory != null) {
                engine = engineFactory.createEvalSyncEngine();
            } else {
                final Eval eval;
                eval = eval(evalName);
                engine = new EvalSyncEngine(eval, matcher.group(2), evalName + "-" + matcher.group(2));
            }
            return new SyncPlayer(engine, depth);
        }
    }

    private static Eval currentEval;

    public static synchronized Eval currentEval() {
        if (currentEval == null) {
            currentEval = eval("ntestJ");
        }
        return currentEval;
    }
}
