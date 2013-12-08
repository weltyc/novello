package com.welty.novello.selfplay;

import com.orbanova.common.misc.Require;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Position;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

/**
 */
public class SelfPlayGame implements Callable<MutableGame> {
    private @NotNull MutableGame game;
    private final @NotNull Player black;
    private final @NotNull Player white;
    private final int gameFlags;
    private final int searchFlags;

    public static final int FLAG_PRINT_GAME = 1;
    public static final int FLAG_MEASURE_TIME = 2;
    /**
     *
     * @param board start position
     * @param black black player
     * @param white white player
     * @param place location of the match (often, Props.getHostName())
     * @param gameFlags  Sum of binary flags defined in SelfPlayGame (FLAG_PRINT_GAME, FLAG_MEASURE_TIME)
     * @param searchFlags  as defined in the Search interface
     */
    public SelfPlayGame(@NotNull Position board, @NotNull Player black, @NotNull Player white, String place, int gameFlags
            , int searchFlags) {
        this.game = new MutableGame(board, black.toString(), white.toString(), place);
        this.black = black;
        this.white = white;
        this.gameFlags = gameFlags;
        this.searchFlags = searchFlags;
    }

    @Override public MutableGame call() {
        while (true) {
            Position board = game.getLastPosition();
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
                    if (printGame()) {
                        System.out.println(game.toGgf());
                        System.out.println("--- result : " + game.getLastPosition().terminalScore());
                    }
                    return game;
                }
            }
        }
    }

    private boolean printGame() {
        return (gameFlags & FLAG_PRINT_GAME)!=0;
    }

    private @NotNull Player player(boolean blackToMove) {
        return blackToMove ? black : white;
    }

    private void move(long moves) {
        final Position board = game.getLastPosition();
        Require.isTrue(moves != 0, "has a move");
        if (printGame()) {
            System.out.println(board.positionString());
            System.out.println();
            System.out.println(board);
            System.out.println(player(board.blackToMove) + " to move");
        }
        final long t0 = measuredTime();
        final MoveScore moveScore = player(board.blackToMove).calcMove(board, moves, searchFlags);
        final long dt = measuredTime()-t0;
        game.play(moveScore, dt*.001);
        if (printGame()) {
            System.out.println("play " + moveScore);
            System.out.println();
        }
    }

    private long measuredTime() {
        if ((gameFlags & FLAG_MEASURE_TIME)!=0) {
            return System.currentTimeMillis();
        } else {
            return 0;
        }
    }
}
