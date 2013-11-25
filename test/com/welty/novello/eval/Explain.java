package com.welty.novello.eval;

import com.welty.novello.selfplay.EvalPlayer;
import com.welty.novello.selfplay.Players;
import com.welty.novello.solver.BitBoard;

/**
 */
public class Explain {
    public static void main(String[] args) {
        final String evalName = "7A";
        final Eval eval = Players.eval(evalName);

        final BitBoard prev = new BitBoard("-------------O----O--OO---***O*---**OO-*--***O-----OO*----------", false);
        System.out.println(prev);
        final EvalPlayer player = new EvalPlayer(eval, 1);
        player.calcMove(prev, prev.calcMoves(), -1);

        // I really can't understand why it wanted to play G7
        final BitBoard board = new BitBoard("-------------O----O--OO---***O*---**OO-*--***O-----OOOO---------", true);
        System.out.println(board);

        System.out.println("Explaining eval. eval() returns " + board.eval(eval) + ".\n\n");

        eval.explain(board.mover(), board.enemy(), board.calcMoves(), board.enemyMoves());
//        CoefficientViewer.dumpSlice(evalName, board.nEmpty(), 0);
    }
}
