package com.welty.novello.eval;

import com.welty.novello.selfplay.Players;
import com.welty.novello.solver.BitBoard;

/**
 */
public class Explain {
    public static void main(String[] args) {
        final String evalName = "7A";

        // I really can't understand why it wanted to play H2
        final BitBoard board = new BitBoard("-------- -------* --O*-**- --O***** --OOO**- -OOOOO-* ---*O--- -----O--", false);
        System.out.println(board);

        final Eval eval = Players.eval(evalName);
        System.out.println("Explaining eval. eval() returns " + board.eval(eval) + ".\n\n");

        eval.explain(board.mover(), board.enemy(), board.calcMoves(), board.enemyMoves());
        CoefficientViewer.dumpSlice(evalName, board.nEmpty(), 0);
    }
}
