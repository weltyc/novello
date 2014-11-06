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

package com.welty.novello.selfplay;

import com.orbanova.common.misc.Require;
import com.welty.novello.core.Board;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.MutableGame;
import com.welty.othello.gdk.OsClock;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

/**
 */
public class SelfPlayGame implements Callable<MutableGame> {
    private @NotNull MutableGame game;
    private final @NotNull SyncPlayer black;
    private final @NotNull SyncPlayer white;
    private final int gameFlags;

    public static final int FLAG_PRINT_GAME = 1;

    /**
     * @param board     start position
     * @param black     black player
     * @param white     white player
     * @param clock     time per player
     * @param place     location of the match (often, Props.getHostName())
     * @param gameFlags Sum of binary flags defined in SelfPlayGame (FLAG_PRINT_GAME, FLAG_MEASURE_TIME)
     */
    public SelfPlayGame(@NotNull Board board, @NotNull SyncPlayer black, @NotNull SyncPlayer white, OsClock clock
            , String place, int gameFlags) {
        this.game = new MutableGame(board, black.toString(), white.toString(), place, clock, clock);
        this.black = black;
        this.white = white;
        this.gameFlags = gameFlags;
    }

    @Override public MutableGame call() {
        black.clear();
        white.clear();
        while (true) {
            Board board = game.getLastBoard();
            final long moverMoves = board.calcMoves();
            if (moverMoves != 0) {
                move(moverMoves);
            } else {
                final long enemyMoves = board.enemyMoves();
                if (enemyMoves != 0) {
                    game.pass();
                    move(enemyMoves);
                } else {
                    game.finish();
                    if (printGame()) {
                        System.out.println(game.toGgf());
                        System.out.println("--- result : " + game.getLastBoard().terminalScoreToBlack());
                    }
                    return game;
                }
            }
        }
    }

    private boolean printGame() {
        return (gameFlags & FLAG_PRINT_GAME) != 0;
    }

    private @NotNull SyncPlayer player(boolean blackToMove) {
        return blackToMove ? black : white;
    }

    private void move(long moves) {
        final Board board = game.getLastBoard();
        Require.isTrue(moves != 0, "has a move");
        if (printGame()) {
            System.out.println(board.positionString());
            System.out.println();
            System.out.println(board);
            System.out.println(player(board.blackToMove) + " to move");
        }
        final long t0 = measuredTime();
        final MoveScore moveScore = player(board.blackToMove).calcMove(game);
        final long dt = measuredTime() - t0;
        game.play(moveScore, dt * .001);
        if (printGame()) {
            System.out.println("play " + moveScore);
            System.out.println();
        }
    }

    private long measuredTime() {
        return System.currentTimeMillis();
    }
}
