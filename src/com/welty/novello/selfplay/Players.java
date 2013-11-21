package com.welty.novello.selfplay;

import com.welty.novello.eval.EvalStrategies;
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
}
