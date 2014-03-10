package com.welty.novello.eval;

import com.welty.novello.core.Board;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.novello.selfplay.Players;

/**
 */
public class Explain {
    public static void main(String[] args) {
        final String evalName = "7B";
        final CoefficientEval eval = (CoefficientEval) Players.eval(evalName);
        final EvalSyncEngine player = new EvalSyncEngine(eval, "", evalName);

        final Board prev = Board.of("--------\n" +
                "--------\n" +
                "-----*--\n" +
                "---***--\n" +
                "---***--\n" +
                "---OO*--\n" +
                "------*-\n" +
                "--------\n" +
                "O");
        System.out.println(prev);
        player.calcMove(prev, null, 1);

        final String moves = "E3,G3";
        for (String move : moves.split(",")) {
            System.out.println("----- " + move + " -----");
            final Board board = prev.play(move);
            System.out.println(board);

            System.out.println("Explaining eval. eval() returns " + eval.eval(board) + ".\n\n");

            eval.explain(board.mover(), board.enemy(), board.calcMoves(), board.enemyMoves());
        }
    }
}
