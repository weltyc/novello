package com.welty.novello.selfplay;

import com.orbanova.common.misc.Require;
import com.welty.novello.solver.BitBoard;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

/**
 */
public class SelfPlayGame implements Callable<MutableGame> {
    private @NotNull MutableGame game;
    private final @NotNull Player black;
    private final @NotNull Player white;
    private final boolean printGame;
    private final int searchFlags;

    public SelfPlayGame(@NotNull BitBoard board, @NotNull Player black, @NotNull Player white, String place, boolean printGame
            , int searchFlags) {
        this.game = new MutableGame(board, black.toString(), white.toString(), place);
        this.black = black;
        this.white = white;
        this.printGame = printGame;
        this.searchFlags = searchFlags;
    }

    @Override public MutableGame call() {
        while (true) {
            BitBoard board = game.getLastPosition();
            final long moverMoves = board.calcMoves();
            if (moverMoves != 0) {
                move(moverMoves);
            } else {
                final long enemyMoves = board.enemyMoves();
                if (enemyMoves!=0) {
                    game.pass();
                    move(enemyMoves);
                }
                else {
                    game.finish();
                    if (printGame) {
                        System.out.println(game.toGgf());
                        System.out.println("--- result : " + game.getLastPosition().netDisks());
                    }
                    return game;
                }
            }
        }
    }

    private @NotNull Player player(boolean blackToMove) {
        return blackToMove ? black : white;
    }

    private void move(long moves) {
        final BitBoard board = game.getLastPosition();
        Require.isTrue(moves != 0, "has a move");
        if (printGame) {
            System.out.println(board.positionString());
            System.out.println();
            System.out.println(board);
            System.out.println(player(board.blackToMove) + " to move");
        }
        final MoveScore moveScore = player(board.blackToMove).calcMove(board, moves, searchFlags);
        game.play(moveScore, 0);
        if (printGame) {
            System.out.println("play " + moveScore);
            System.out.println();
        }
    }
}
