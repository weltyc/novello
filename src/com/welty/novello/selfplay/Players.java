package com.welty.novello.selfplay;

import com.welty.novello.eval.Eval;
import com.welty.novello.eval.EvalStrategies;
import com.welty.novello.eval.EvalStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class containing Othello players
 */
public class Players {
    private static Map<String, Eval> namedEvals = new HashMap<>();
    static {
        namedEvals.put("Charlie", Charlie.charlieEval);
    }

    public static Eval eval(String name) {
        final Eval eval = namedEvals.get(name);
        if (null!=eval) {
            return eval;
        } else {
            EvalStrategy strategy = EvalStrategies.strategy(name.substring(0, 1));
            return new Eval(strategy, name.substring(1));
        }
    }

    public static Player player(String name) {
        final String [] parts = name.split(":",2);
        final int depth = parts.length > 1 ? Integer.parseInt(parts[1]):1;
        final Eval eval = eval(parts[0]);
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
}
