package com.welty.novello.selfplay;

import com.welty.novello.eval.PositionValue;
import com.welty.novello.solver.BitBoard;
import com.welty.novello.solver.BitBoardUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 */
public class SelfPlayGame implements Callable<SelfPlayGame.Result> {
    private @NotNull BitBoard board;
    private final @NotNull Player black;
    private final @NotNull Player white;
    private final boolean printGame;
    private final int searchFlags;

    // store positions encountered in the game, but only positions with a legal move.
    // the evaluator will only evaluate positions where the mover has a legal move.
    private final @NotNull List<BitBoard> blackToMovePositions = new ArrayList<>();
    private final @NotNull List<BitBoard> whiteToMovePositions = new ArrayList<>();

    public SelfPlayGame(@NotNull BitBoard board, @NotNull Player black, @NotNull Player white, boolean printGame
            , int searchFlags) {
        this.board = board;
        this.black = black;
        this.white = white;
        this.printGame = printGame;
        this.searchFlags = searchFlags;
    }

    @Override public Result call() {
        while (true) {
            if (printGame) {
                System.out.println(board.boardString());
                System.out.println();
                System.out.println(board);
                System.out.println(player(board.blackToMove) + " to move");
            }
            if (!moveIfLegal()) {
                board = board.pass();
                if (!moveIfLegal()) {
                    final Result result = new Result(board.netDisks(), blackToMovePositions, whiteToMovePositions);
                    if (printGame) {
                        System.out.println("--- result : " + result);
                    }
                    return result;
                }
            }
        }
    }

    private @NotNull Player player(boolean blackToMove) {
        return blackToMove ? black : white;
    }

    private boolean moveIfLegal() {
        final long moverMoves = board.calcMoves();
        final boolean result = moverMoves!=0;
        if (result) {
            (board.blackToMove ? blackToMovePositions : whiteToMovePositions).add(board);
            final int sq = player(board.blackToMove).calcMove(board, moverMoves, searchFlags);
            board = board.play(sq);
            if (printGame) {
                System.out.println("play " + BitBoardUtils.sqToText(sq));
                System.out.println();
            }
        }
        return result;
    }

    /**
     * Result of an othello game.
     * <p/>
     * Also has the positions of the game where the mover has a legal move.
     */
    public static class Result {
        /**
         * Net score (black disks - white disks).
         */
        public final int netScore;
        private final @NotNull List<BitBoard> blackToMovePositions;
        private final @NotNull List<BitBoard> whiteToMovePositions;

        private Result(int netScore, @NotNull List<BitBoard> blackToMovePositions, @NotNull List<BitBoard> whiteToMovePositions) {
            this.netScore = netScore;
            this.blackToMovePositions = blackToMovePositions;
            this.whiteToMovePositions = whiteToMovePositions;
        }

        @Override public String toString() {
            return (netScore > 0 ? "+" : "") + netScore;
        }

        public List<PositionValue> getPositionValues() {
            final List<PositionValue> pvs = new ArrayList<>();
            for (BitBoard bb : blackToMovePositions) {
                pvs.add(new PositionValue(bb.black, bb.white, netScore));
            }
            for (BitBoard bb : whiteToMovePositions) {
                pvs.add(new PositionValue(bb.white, bb.black, -netScore));
            }
            return pvs;
        }
    }
}
