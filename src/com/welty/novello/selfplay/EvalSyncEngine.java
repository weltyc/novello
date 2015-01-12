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
import com.orbanova.common.misc.Utils;
import com.welty.novello.book.Book;
import com.welty.novello.book.MultithreadedAdder;
import com.welty.novello.core.*;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.*;
import com.welty.othello.api.AbortCheck;
import com.welty.othello.gdk.COsBoard;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsClock;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.protocol.Depth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A SyncEngine that chooses its move using an Eval and a search.
 * <p/>
 * This implementation is NOT thread-safe.
 */
public class EvalSyncEngine implements SyncEngine {
    private final MidgameSearcher.Options midgameOptions;
    private final MidgameSearcher searcher;
    private final Solver solver;
    private final @NotNull String name;
    @Nullable private final Book book;

    public EvalSyncEngine(@NotNull Eval eval, String options, @NotNull String name) {
        this(eval, options, name, null);
    }

    public EvalSyncEngine(@NotNull Eval eval, String options, @NotNull String name, @Nullable Book book) {
        this.name = name;
        this.book = book;
        midgameOptions = new MidgameSearcher.Options(options);
        this.solver = new Solver(eval, midgameOptions, book);
        this.searcher = solver.midgameSearcher;
    }

    @NotNull @Override public MoveScore calcMove(@NotNull MutableGame game, int maxDepth) {
        final Position position = game.getLastPosition();
        return calcMove(position.board, position.board.blackToMove ? position.blackClock : position.whiteClock, maxDepth);
    }

    @NotNull public MoveScore calcMove(@NotNull Board board, @Nullable OsClock clock, int maxDepth) {
        return calcMove(board, clock, maxDepth, AbortCheck.NEVER, Listener.NULL);
    }

    @Override public void clear() {
        solver.clear(64);
        searcher.clear();
    }

    @Override public String toString() {
        return name;
    }

    /**
     * Calc the move that the engine would like to play
     *
     * @param board           position to search from
     * @param clock           amount of time remaining, or null if the player should ignore the clock
     * @param maxMidgameDepth maximum search depth, during midgame
     * @param baseAbortCheck  abort check (not including time caps, which are calculated inside this method)
     * @param listener        search status listener
     * @return the move the engine would like to play and its score.
     */
    public MoveScore calcMove(Board board, @Nullable OsClock clock, int maxMidgameDepth, AbortCheck baseAbortCheck, Listener listener) {
        final long moverMoves = board.calcMoves();
        if (moverMoves == 0) {
            throw new IllegalArgumentException("Must have a legal move to call calcMove()");
        }

        MoveScore bookMoveScore = getBookMoveScore(board, maxMidgameDepth);
        if (bookMoveScore != null) {
            return bookMoveScore;
        }
        if (midgameOptions.variableMidgame && board.nEmpty() <= 30) {
            maxMidgameDepth += 2;
        }
        final AbortCheck abortCheck = clock == null ? baseAbortCheck : new TimeAbortCheck(baseAbortCheck, clock, board.nEmpty());

        final long n0 = searcher.getCounts().nFlips;
        final long t0 = System.currentTimeMillis();

        // Make sure we have a legal move by calculating the first round without aborts
        MoveScore result = null;
        boolean firstRound = true;

        //  calculate further rounds with aborts enabled
        for (SearchDepth searchDepth : SearchDepths.calcSearchDepths(board.nEmpty(), maxMidgameDepth)) {
            if (!firstRound && abortCheck.abortNextRound()) {
                break;
            }
            try {
                final AbortCheck roundAbortCheck = firstRound ? AbortCheck.NEVER : abortCheck;
                listener.updateStatus(status(searchDepth));
                if (searchDepth.isFullSolve()) {
                    // full-width solve
                    result = solver.getMoveScore(board.mover(), board.enemy(), roundAbortCheck, new MyStatsListener(listener, n0, t0, solver));
                } else {
                    result = searcher.getMoveScore(board, moverMoves, searchDepth.depth, searchDepth.width, roundAbortCheck);
                    listener.hint(result, searchDepth.displayDepth(), false);
                }
            } catch (SearchAbortedException e) {
                listener.updateStatus("Round aborted");
                listener.updateNodeStats(searcher.getCounts().nFlips - n0, System.currentTimeMillis() - t0);
            }
            listener.updateNodeStats(searcher.getCounts().nFlips - n0, System.currentTimeMillis() - t0);
            firstRound = false;
        }
        return result;
    }

    /**
     * Get move score from book
     *
     * @return move score from book, or null if no move score can be determined
     */
    private MoveScore getBookMoveScore(Board board, int maxMidgameDepth) {
        if (book==null) {
            return null;
        }
        final Book.Data data = book.getData(board);
        if (data==null) {
            return null;
        }
        switch(data.getNodeType()) {
            case ULEAF:
                return null;
            case UBRANCH:
                // if this search requires a solve, then don't return an unsolved value.
                if (SearchDepths.lastSearchDepth(board.nEmpty(), maxMidgameDepth).isFullSolve()) {
                    return null;
                }
                break;
        }
        final List<Book.Successor> successors = book.getSuccessors(board);
        if (hasMatchingScore(data, successors)) {
            for (Book.Successor s : successors) {
                if (data.getScore()==s.score) {
                    return new MoveScore(s.sq, s.score * CoefficientCalculator.DISK_VALUE);
                }
            }
        }
        return null;
    }

    static double calcTargetTime(OsClock clock, int nEmpty) {
        final int baseNEmpty = Utils.isOdd(nEmpty) ? 1 : 2;
        double tTarget = 0;

        for (int solveAt = baseNEmpty; solveAt <= nEmpty; solveAt += 2) {
            final double tSolve = 120 * Math.pow(solveAt > 26 ? 5 : 4, solveAt - 26);
            if (tSolve > clock.tCurrent) {
                break;
            }
            final int nMidgameMoves = 1 + (nEmpty - solveAt) / 2;
            final double tMidgame = (clock.tCurrent - tSolve) / nMidgameMoves;
            if (tMidgame > tTarget) {
                tTarget = tMidgame;
            }
            if (tMidgame < tSolve) {
                break;
            }
        }
        return Math.min(tTarget, clock.tCurrent / 4);
    }

    private static class TimeAbortCheck implements AbortCheck {
        private final AbortCheck chainedAbortCheck;
        private final long tNoMoreRounds;
        private final long tHardAbort;

        TimeAbortCheck(AbortCheck chainedAbortCheck, @NotNull OsClock clock, int nEmpty) {
            this.chainedAbortCheck = chainedAbortCheck;
            final long now = System.currentTimeMillis();
            final double tTarget = calcTargetTime(clock, nEmpty);
            tHardAbort = now + cap(tTarget * 2, clock.tCurrent);
            tNoMoreRounds = now + cap(tTarget * 0.5, clock.tCurrent);
//            System.out.println("now: " + now);
//            System.out.println("clock.tCurrent: " + clock.tCurrent);
//            System.out.println("nEmpty: " + nEmpty);
//            System.out.println("tHardAbort: +" + (tHardAbort - now));
//            System.out.println("tNoMoreRounds: +" + (tNoMoreRounds - now));
        }

        private static long cap(double t, double tCurrent) {
            if (t >= tCurrent) {
                t = tCurrent * 0.8;
            }
            return Math.max(1, (long) (1000 * t));
        }

        @Override public boolean shouldAbort() {
            return System.currentTimeMillis() >= tHardAbort || chainedAbortCheck.shouldAbort();
        }

        @Override public boolean abortNextRound() {
            return System.currentTimeMillis() >= tNoMoreRounds || chainedAbortCheck.abortNextRound();
        }
    }

    public void analyze(COsGame game, int maxDepth, AbortCheck abortCheck, Listener listener) {
        final int nMoves = game.nMoves();

        // retrograde analysis.
        // The terminal value is calculated from the net disks of the game, if the game is over. Otherwise from a search.
        // It is positive if the mover is ahead.
        double score = calcScoreToBlack(maxDepth, game.pos.board, abortCheck);
        if (abortCheck.shouldAbort()) {
            return;
        }
        listener.analysis(game.nMoves(), score);

        for (int i = nMoves - 1; i >= 0; i--) {
            final COsBoard board = game.PosAtMove(i).board;
            final Board position = Board.of(board);
            final long moverMoves = position.calcMoves();

            if (Long.bitCount(moverMoves) > 1) {
                // The move is not forced. Do a search. If the search move is the same as the played move, return the
                // evaluation from the next position with the sign flipped. Otherwise, return the result of the eval.
                final MoveScore moveScore = calcMove(position, null, maxDepth, abortCheck, Listener.NULL);
                if (abortCheck.shouldAbort()) {
                    return;
                }
                final OsMove playedMove = game.getMli(i).move;
                // need to use 7-row and 7-col because osMove orders row and col differently than moveScore.
                final int playedSq = BitBoardUtils.square(7 - playedMove.row(), 7 - playedMove.col());
                if (moveScore.sq != playedSq) {
                    final double newScore = 0.01 * moveScore.centidisks * moverSign(board);
                    System.out.println("move " + (i + 1) + " played " + BitBoardUtils.sqToText(playedSq) + " recommended " + BitBoardUtils.sqToText(moveScore.sq) + ", lost " + Math.abs(score - newScore));
                    score = newScore;
                }
                listener.analysis(i, score);
            }
        }
    }

    private double calcScoreToBlack(int maxDepth, COsBoard board, AbortCheck abortCheck) {
        Board position = Board.of(board);
        final double scoreToBlack;
        final double moverSign = moverSign(board);
        if (position.hasLegalMove()) {
            final MoveScore moveScore = calcMove(position, null, maxDepth, abortCheck, Listener.NULL);
            scoreToBlack = moveScore.centidisks * 0.01 * moverSign;
        } else {
            position = position.pass();
            if (position.hasLegalMove()) {
                final MoveScore moveScore = calcMove(position, null, maxDepth, abortCheck, Listener.NULL);
                scoreToBlack = moveScore.centidisks * -0.01 * moverSign;
            } else {
                scoreToBlack = board.netBlackSquares();
            }
        }
        return scoreToBlack;
    }

    private static int moverSign(COsBoard board) {
        return board.isBlackMove() ? +1 : -1;
    }

    public void learn(COsGame game, int maxDepth, AbortCheck abortCheck, Listener listener) {
        MultithreadedAdder adder = new MultithreadedAdder(book, maxDepth);
        book.learn(game, adder);
    }

    /**
     * Evaluate the top legal moves and provide updates during a search.
     * <p/>
     * Only called if there is at least one legal move from this position
     *
     * @param listener listener for intermediate results
     */
    public void calcHints(Board board, int maxMidgameDepth, int nHints, AbortCheck abortCheck, Listener listener) throws SearchAbortedException {
        if (getHintsFromBook(board, listener)) {
            return;
        }
        final long moverMoves = board.calcMoves();
        if (moverMoves == 0) {
            throw new IllegalArgumentException("Must have a legal move to call calcHints()");
        }
        final long n0 = searcher.getCounts().nFlips;
        final long t0 = System.currentTimeMillis();

        // list of moveScores, sorted in descending order by score for at least the first nHints scores
        final ArrayList<MoveScore> moveScores = new ArrayList<>();
        long moves = board.calcMoves();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            moveScores.add(new MoveScore(sq, 0));
            moves &= moves - 1;
        }

        for (SearchDepth searchDepth : SearchDepths.calcSearchDepths(board.nEmpty(), maxMidgameDepth)) {
            listener.updateStatus(status(searchDepth));
            for (int j = 0; j < moveScores.size(); j++) {
                final int beta = 64 * CoefficientCalculator.DISK_VALUE;
                final int alpha = j < nHints ? -64 * CoefficientCalculator.DISK_VALUE : moveScores.get(nHints - 1).centidisks;
                final int sq = moveScores.get(j).sq;
                final MoveScore moveScore;
                if (searchDepth.isFullSolve()) {
                    final int solverAlpha = MidgameSearch.solverAlpha(alpha);
                    final int solverBeta = MidgameSearch.solverBeta(beta);

                    moveScore = solver.calcSubMoveScore(sq, board, solverAlpha, solverBeta, abortCheck, new MyStatsListener(listener, n0, t0, solver));
                } else {
                    moveScore = searcher.calcSubMoveScore(sq, board, alpha, beta, searchDepth.depth - 1, searchDepth.width, abortCheck);
                }
                if (moveScore.centidisks > alpha || moveScore.centidisks == -64 * CoefficientCalculator.DISK_VALUE) {
                    insertSorted(moveScores, j, moveScore);
                    listener.hint(moveScore, searchDepth.displayDepth(), false);
                } else {
                    moveScores.set(j, moveScore);
                }
                listener.updateNodeStats(searcher.getCounts().nFlips - n0, System.currentTimeMillis() - t0);
            }
        }
    }

    // Depths that are displayed in NBoard for various book node types
    private static final Depth uLeafDepth = new Depth(0);
    private static final Depth uBranchDepth = new Depth(1);
    private static final Depth solveDepth = new Depth("100%");
    private static Depth depthFromNodeType(Book.NodeType nodeType) {
        switch(nodeType) {
            case ULEAF:
                return uLeafDepth;
            case UBRANCH:
                return uBranchDepth;
            case SOLVED:
                return solveDepth;
            default:
                throw new IllegalArgumentException("Unknown node type : " + nodeType);
        }
    }
    /**
     * Get hints from book, if possible
     *
     * @param board    board to search for. There must be at least one legal move.
     * @param listener listener for book hints.
     * @return true if hints were gotten from the book.
     */
    private boolean getHintsFromBook(Board board, Listener listener) {
        if (book == null || book.minDepth() >= board.nEmpty()) {
            return false;
        }
        final Book.Data data = book.getData(board);
        if (data == null || data.getNodeType() == Book.NodeType.ULEAF) {
            return false;
        }
        List<Book.Successor> successors = book.getSuccessors(board);
        if (data.getNodeType() == Book.NodeType.SOLVED && !hasMatchingScore(data, successors)) {
            return false;
        }

        for (Book.Successor s : successors) {
            listener.hint(new MoveScore(s.sq, s.score * CoefficientCalculator.DISK_VALUE),depthFromNodeType(s.nodeType), true);
        }
        return true;
    }

    private boolean hasMatchingScore(Book.Data data, List<Book.Successor> successors) {
        for (Book.Successor s : successors) {
            if (data.getScore() == s.score) {
                return true;
            }
        }
        return false;
    }

    private static String status(SearchDepth searchDepth) {
        return searchDepth.isFullSolve() ? "Solving..." : "Searching at " + searchDepth;
    }

    /**
     * Insert the moveScore in the list so that it remains sorted by moveScore.
     */
    static void insertSorted(ArrayList<MoveScore> moveScores, int j, MoveScore moveScore) {
        Require.eq(moveScores.get(j).sq, "array move", moveScore.sq, "moveScore move");
        int k;
        for (k = j - 1; k >= 0; k--) {
            if (moveScores.get(k).centidisks >= moveScore.centidisks) {
                break;
            }
            moveScores.set(k + 1, moveScores.get(k));
        }
        moveScores.set(k + 1, moveScore);
    }

    public interface Listener {
        Listener NULL = new Listener() {
            @Override public void updateStatus(String status) {
            }

            @Override public void updateNodeStats(long nodeCount, long millis) {
            }

            @Override public void hint(MoveScore moveScore, Depth depth, boolean isBook) {
            }

            @Override public void analysis(int moveNumber, double eval) {
            }
        };

        void updateStatus(String status);

        void updateNodeStats(long nodeCount, long millis);

        void hint(MoveScore moveScore, Depth depth, boolean isBook);

        /**
         * The engine is reporting the results of a retrograde analysis of a game.
         *
         * @param moveNumber number of moves that have been played in the game.
         * @param eval       evaluation; positive numbers mean black is ahead.
         */
        void analysis(int moveNumber, double eval);
    }

    private class MyStatsListener implements StatsListener {
        private final Listener listener;
        private final long n0;
        private final long t0;
        private final Solver solver;

        private long nextUpdateMillis;
        private static final long INITIAL_UPDATE_MILLIS = 500;

        public MyStatsListener(Listener listener, long n0, long t0, Solver solver) {
            this.listener = listener;
            this.n0 = n0;
            this.t0 = t0;
            this.solver = solver;
            nextUpdateMillis = System.currentTimeMillis() + INITIAL_UPDATE_MILLIS;
        }

        @Override public void update() {
            final long now = System.currentTimeMillis();
            if (now > nextUpdateMillis) {
                final long elapsedMillis = now - t0;
                listener.updateNodeStats(solver.getCounts().nFlips - n0, elapsedMillis);
                nextUpdateMillis = now + updateInterval(elapsedMillis);
            }
        }

        private long updateInterval(long elapsedMillis) {
            final double formula = Math.sqrt(elapsedMillis / (double) INITIAL_UPDATE_MILLIS);
            return (long) (INITIAL_UPDATE_MILLIS * Math.max(1., formula));
        }
    }
}
