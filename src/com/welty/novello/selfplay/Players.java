package com.welty.novello.selfplay;

import com.welty.novello.eval.CoefficientEval;
import com.welty.novello.eval.Eval;
import com.welty.novello.eval.EvalStrategies;
import com.welty.novello.eval.EvalStrategy;
import com.welty.novello.ntest.NTest;

/**
 * Utility class containing Othello players
 */
public class Players {
    public static CoefficientEval eval(String name) {
        EvalStrategy strategy = EvalStrategies.strategy(name.substring(0, 1));
        return new CoefficientEval(strategy, name.substring(1));
    }

    public static Player player(String name) {
        final String [] parts = name.split(":",2);
        final int depth = parts.length > 1 ? Integer.parseInt(parts[1]):1;
        final String evalName = parts[0];
        if (evalName.equals("ntest")) {
            return new NTest(depth, false);
        }
        final Eval eval = eval(evalName);
        return new EvalPlayer(eval, depth);
    }

    /**
     * Generates a list of Players from a text string.
     * <p/>
     * The text string is a list of players separated by commas, for example "4A,5B,5C".
     * the first character of each player is the EvaluationStrategy; the second is the coefficient set.
     *
     * @param s players list
     * @return Players
     */
    static Player[] players(String s) {
        final String[] names = s.trim().split("\\s*,\\s*");
        final Player[] players = new Player[names.length];
        for (int i = 0; i < names.length; i++) {
            players[i] = player(names[i]);
        }
        return players;
    }

    public static Eval currentEval() {
        return eval("b1");
    }
}
