package com.welty.novello.selfplay;

import com.welty.novello.eval.EvalStrategies;
import com.welty.novello.eval.EvalStrategy;
import com.welty.novello.eval.StrategyBasedEval;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class containing Othello players
 */
public class Players {
    public static final Player bobby = new Bobby();

    private static Player eval4A;

    public static Player eval4A() {
        if (eval4A == null) {
            eval4A = new EvalPlayer(new StrategyBasedEval(EvalStrategies.eval4, "A"));
        }
        return eval4A;
    }

    private static Player eval5A;

    public static synchronized @NotNull Player eval5A() {
        if (eval5A == null) {
            eval5A = new EvalPlayer(new StrategyBasedEval(EvalStrategies.eval5, "A"));
        }
        return eval5A;
    }

    private static Player eval5B;

    public static Player eval5B() {
        if (eval5B == null) {
            eval5B = new EvalPlayer(new StrategyBasedEval(EvalStrategies.eval5, "B"));
        }
        return eval5B;
    }

    static Player player(String name) {
        EvalStrategy strategy = EvalStrategies.strategy(name.substring(0, 1));
        final StrategyBasedEval eval = new StrategyBasedEval(strategy, name.substring(1));
        return new EvalPlayer(eval);
    }

    /**
     * Generates a list of Players from a text string.
     *
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
