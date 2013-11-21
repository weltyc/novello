package com.welty.novello.selfplay;

import com.welty.novello.eval.EvalStrategies;
import com.welty.novello.eval.StrategyBasedEval;

/**
 * Utility class containing Othello players
 */
public class Players {
    public static final Player bobby = new Bobby();
    public static final Player eval4 = new EvalPlayer(new StrategyBasedEval(EvalStrategies.eval4, "A"));
    public static final Player eval5a = new EvalPlayer(new StrategyBasedEval(EvalStrategies.eval5, "A"));
}
