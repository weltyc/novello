package com.welty.novello.selfplay;

import com.welty.novello.eval.CoefficientEval;
import com.welty.novello.eval.Eval;
import com.welty.novello.eval.EvalStrategies;
import com.welty.novello.eval.EvalStrategy;
import com.welty.novello.ntest.NBoardPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class containing Othello players
 */
public class Players {
    public static CoefficientEval eval(String name) {
        EvalStrategy strategy = EvalStrategies.strategy(name.substring(0, 1));
        return new CoefficientEval(strategy, name.substring(1));
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
        if (evalName.equals("ntest") || evalName.equals("edax")) {
            return new NBoardPlayer(evalName, depth, false);
        }
        final Eval eval = eval(evalName);
        return new EvalPlayer(eval, depth, matcher.group(2));
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

    private static Eval currentEval;

    public static synchronized Eval currentEval() {
        if (currentEval == null) {
            currentEval = eval("c1s");
        }
        return currentEval;
    }
}
