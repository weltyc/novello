package com.welty.novello.selfplay;

import com.welty.novello.eval.*;
import com.welty.novello.ntest.NBoardPlayer;
import com.welty.ntestj.CEvaluatorJ;

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
     * Construct a Player from a text string
     * <p/>
     * The text string has the format
     * {eval}:{depth}
     * <p/>
     * {eval} is an evaluation strategy followed by a coefficient set, for example "c4s"
     * <p/>
     * {depth} is an integer, optionally followed by 'w' for full width, for example "8" or "5w". If 'w'
     * is not specified, the Player will use MPC.
     * <p/>
     * {eval} may also be "ntest", in which case an external NTest process is launched. NTest always
     * uses MPC regardless of trailing 'w'.
     *
     * @param textString player text string
     * @return a newly constructed Player.
     */
    public static Player player(String textString) {
        final String[] parts = textString.split(":", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("require an eval and a search depth, for instance 'a1:3w'; had " + textString);
        }

        final Pattern pattern = Pattern.compile("([0-9]+)([a-z]*)");
        final Matcher matcher = pattern.matcher(parts[1]);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal depth and options: " + parts[1]);
        }

        final int depth = Integer.parseInt(matcher.group(1));

        final String evalName = parts[0];
        if (evalName.equals("ntest") || evalName.equals("edax") || evalName.equals("ntest-new")) {
            return new NBoardPlayer(evalName, depth, false);
        }
        final Eval eval;
        eval = eval(evalName);
        return new EvalPlayer(eval, depth, matcher.group(2));
    }

    /**
     * Generates a list of Players from a text string.
     * <p/>
     * Each element of names[] is the name of a player, for instance "d1s:3".
     * the first character of each player is the EvaluationStrategy; the second is the coefficient set.
     *
     * @param names players list
     * @return Players
     */
    static Player[] players(String[] names) {
        final Player[] players = new Player[names.length];
        for (int i = 0; i < names.length; i++) {
            players[i] = player(names[i]);
        }
        return players;
    }

    private static Eval currentEval;

    public static synchronized Eval currentEval() {
        if (currentEval == null) {
            currentEval = eval("ntestJ");
        }
        return currentEval;
    }
}
