package com.welty.novello.solver;

import com.welty.novello.core.*;
import com.welty.othello.api.AbortCheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 */
public class EndgameAnalyzer {
    private static final Solver solver = new Solver();

    public static void main(String[] args) throws IOException {
        System.out.println("analyzes mistakes in a GGF game");
        System.out.println("enter games (in GGF format) in stdin\n");
        String line;
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (null != (line = in.readLine())) {
            final MutableGame game = MutableGame.ofGgf(line.trim());
            analyzeGame(game);
        }
    }

    private static void analyzeGame(MutableGame game) {
        final int[] dropped = new int[2];

        final List<Move8x8> moves = game.getMoves();
        Position position = game.getStartPosition();
        for (Move8x8 move : moves) {
            final int nEmpty = position.nEmpty();
            if (nEmpty <= 22 && position.calcMoves() != 0) {
                final MoveScore best = solver.getMoveScore(position.mover(), position.enemy());
                Position next = position.play(move.getSq());
                final int score = -solver.solve(next.mover(), next.enemy());

                if (best.centidisks != score) {
                    final int drop = best.centidisks - score;
                    if (drop != 0) {
                        final String playerName = position.blackToMove ? "Black" : "White";
                        final String played = BitBoardUtils.sqToText(move.getSq()) + "/" + score;
                        System.out.format("%2d: %s dropped %2d, played %s should have played %s\n", nEmpty
                                , playerName, drop, played, best);
                        dropped[position.blackToMove ? 0 : 1] += drop;
                    }
                }
            }
            position = position.playOrPass(move.getSq());
        }

        System.out.println();
        System.out.println("Total : Black dropped " + dropped[0] + ", White dropped " + dropped[1]);
    }
}
