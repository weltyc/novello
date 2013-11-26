package com.welty.novello.selfplay;

import com.welty.novello.eval.PositionValue;
import com.welty.novello.solver.BitBoard;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 */
public class SelfPlayGame implements Callable<MutableGame> {
    private @NotNull MutableGame game;
    private final @NotNull Player black;
    private final @NotNull Player white;
    private final boolean printGame;
    private final int searchFlags;

    public SelfPlayGame(@NotNull BitBoard board, @NotNull Player black, @NotNull Player white, boolean printGame
            , int searchFlags) {
        this.game = new MutableGame(board);
        this.black = black;
        this.white = white;
        this.printGame = printGame;
        this.searchFlags = searchFlags;
    }

    @Override public MutableGame call() {
        while (true) {
            if (!moveIfLegal()) {
                game.pass();
                if (!moveIfLegal()) {
                    if (printGame) {
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

    private boolean moveIfLegal() {
        final BitBoard board = game.getLastPosition();
        final long moverMoves = board.calcMoves();
        final boolean canMove = moverMoves != 0;
        if (canMove) {
            if (printGame) {
                System.out.println(board.boardString());
                System.out.println();
                System.out.println(board);
                System.out.println(player(board.blackToMove) + " to move");
            }
            final MoveScore moveScore = player(board.blackToMove).calcMove(board, moverMoves, searchFlags);
            game.play(moveScore, 0);
            if (printGame) {
                System.out.println("play " + moveScore);
                System.out.println();
            }
        }
        return canMove;
    }
}
