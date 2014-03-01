package com.welty.novello.selfplay;

import com.welty.novello.eval.*;
import com.welty.novello.ntest.NBoardSyncEngine;
import com.welty.ntestj.CEvaluatorJ;
import com.welty.othello.gui.ExternalEngineManager;

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
     * @return a newly constructed SyncEngine.
     */
    public static SyncPlayer player(String textString) {
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
            return new SyncPlayer(new NBoardSyncEngine(xei, false), depth);
        } else {
            final Eval eval;
            eval = eval(evalName);
            return new SyncPlayer(new EvalSyncEngine(eval, matcher.group(2)), depth);
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
