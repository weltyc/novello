package com.welty.novello.eval;

import com.welty.novello.selfplay.EvalPlayer;
import com.welty.novello.selfplay.Players;
import com.welty.novello.core.Position;

/**
 */
public class Explain {
    public static void main(String[] args) {
        final String evalName = "7B";
        final Eval eval = Players.eval(evalName);
        final EvalPlayer player = new EvalPlayer(eval, 1);

        // I really can't understand why it wanted to play G7
        // in a two ply search.
        final Position prev = new Position("--------\n" +
                "--------\n" +
                "-----*--\n" +
                "---***--\n" +
                "---***--\n" +
                "---OO*--\n" +
                "------*-\n" +
                "--------\n" +
                "O");
        System.out.println(prev);
        player.calcMove(prev, prev.calcMoves(), -1);

        final String moves = "E3,G3";
        for (String move : moves.split(",")) {
            System.out.println("----- " + move + " -----");
            final Position board = prev.play(move);
            System.out.println(board);

            System.out.println("Explaining eval. eval() returns " + board.eval(eval) + ".\n\n");

            eval.explain(board.mover(), board.enemy(), board.calcMoves(), board.enemyMoves());
        }
    }
}
