/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

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
