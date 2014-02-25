package com.welty.novello.selfplay;

import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Utils;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Eval;
import com.welty.novello.solver.MidgameSearcher;
import com.welty.novello.solver.Solver;
import com.welty.ntestj.Heights;
import com.welty.othello.api.AbortCheck;
import com.welty.othello.gdk.COsBoard;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsClock;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.protocol.Depth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * A SyncEngine that chooses its move using an Eval and a search.
 * <p/>
 * This implementation is NOT thread-safe.
 */
public class EvalSyncEngine implements SyncEngine {
    @NotNull private final Eval eval;
    private final MidgameSearcher.Options midgameOptions;
    private final String options;
    private final MidgameSearcher searcher;
    private final Solver solver;

    public EvalSyncEngine(@NotNull Eval eval, String options) {
        this.eval = eval;
        this.options = options;
        midgameOptions = new MidgameSearcher.Options(options);
        this.solver = new Solver(eval, midgameOptions);
        this.searcher = solver.midgameSearcher;
    }

    public MoveScore calcMove(@NotNull Position position, @Nullable OsClock clock, int maxDepth) {
        return calcMove(position, clock, maxDepth, AbortCheck.NEVER, Listener.NULL);
    }

    @Override public void clear() {
        solver.clear(64);
        searcher.clear();
    }

    @Override public String toString() {
        return eval + "-" + options;
    }

    /**
     * Calc the move that the engine would like to play
     *
     * @param position
     * @param clock      amount of time remaining, or null if the player should ignore the clock
     * @param maxDepth
     * @param baseAbortCheck
     * @param listener
     * @return the move the engine would like to play and its score.
     */
    public MoveScore calcMove(Position position, @Nullable OsClock clock, int maxDepth, AbortCheck baseAbortCheck, Listener listener) {
        final long moverMoves = position.calcMoves();
        if (moverMoves == 0) {
            throw new IllegalArgumentException("Must have a legal move to call calcMove()");
        }

        final AbortCheck abortCheck = clock==null ? baseAbortCheck : new TimeAbortCheck(baseAbortCheck, clock, position.nEmpty());

        final long n0 = searcher.getCounts().nFlips;
        final long t0 = System.currentTimeMillis();

        final MoveScore result;
        if (shouldSolve(position, maxDepth)) {
            listener.updateStatus("Solving...");
            // full-width solve
            result = solver.getMoveScore(position.mover(), position.enemy(), abortCheck);
        } else {
            final Depths depths = calcSearchDepth(position, maxDepth);
            listener.updateStatus("Searching at " + depths.displayDepth().humanString());
            result = searcher.getMoveScore(position, moverMoves, depths.searchDepth, abortCheck);
        }
        listener.updateNodeStats(searcher.getCounts().nFlips - n0, System.currentTimeMillis() - t0);
        return result;
    }

    private static double calcTargetMillis(OsClock clock, int nEmpty) {
        final int baseNEmpty = Utils.isOdd(nEmpty) ? 1 : 2;
        double tTarget = 0;

        for (int solveAt = baseNEmpty; solveAt <= nEmpty; solveAt++) {
            final double tSolve = 60 * Math.pow(5, solveAt);
            if (tSolve > clock.tCurrent) {
                break;
            }
            final int nMidgameMoves = (nEmpty - solveAt) / 2;
            if (nMidgameMoves == 0) {
                tTarget = tSolve;
                break;
            }
            final double tMidgame = (clock.tCurrent - tSolve) / nMidgameMoves;
            if (tMidgame < tSolve) {
                tTarget = tMidgame;
            } else {
                break;
            }
        }
        return tTarget;
    }

    private static class TimeAbortCheck implements AbortCheck {
        private final AbortCheck chainedAbortCheck;
        private final long tNoMoreRounds;
        private final long tHardAbort;

        TimeAbortCheck(AbortCheck chainedAbortCheck, @NotNull OsClock clock, int nEmpty) {
            this.chainedAbortCheck = chainedAbortCheck;
            final long now = System.currentTimeMillis();
            final double tTarget = calcTargetMillis(clock, nEmpty);
            tHardAbort = now + cap(tTarget*2, clock.tCurrent);
            tNoMoreRounds = now + cap(tTarget*0.5, clock.tCurrent);
        }

        private static long cap(double t, double tCurrent) {
            if (t>=tCurrent) {
                t = tCurrent * 0.8;
            }
            return Math.max(1, (long)(1000*t));
        }

        @Override public boolean shouldAbort() {
            return System.currentTimeMillis() >= tHardAbort;
        }

        @Override public boolean abortNextRound() {
            return System.currentTimeMillis() >= tNoMoreRounds;
        }
    }

    final Depths calcSearchDepth(Position position, int maxDepth) {
        final int nEmpty = position.nEmpty();
        final int solverStart = midgameOptions.useSolver ? MidgameSearcher.SOLVER_START_DEPTH - 1 : 0;

        final int probableSolveHeight;
        if (midgameOptions.useNtestSearchDepths) {
            final Heights heights = new Heights(maxDepth);
            probableSolveHeight = heights.getProbableSolveHeight();
        } else {
            probableSolveHeight = maxDepth;
        }

        if (nEmpty <= probableSolveHeight) {
            // probable solve
            return new Depths(nEmpty - solverStart, true);
        } else {
            return new Depths(maxDepth, false);
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
            final Position position = Position.of(board);
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
        Position position = Position.of(board);
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
    }

    static class Depths {
        /**
         * Depth passed into midgame searcher
         */
        final int searchDepth;

        /**
         * If true, a search at the searchDepth
         */
        final boolean isProbable;

        private Depths(int searchDepth, boolean probable) {
            this.searchDepth = searchDepth;
            isProbable = probable;
        }

        private Depth displayDepth() {
            return isProbable ? new Depth("60%") : new Depth(searchDepth);
        }
    }

    /**
     * Only called if there is at least one legal move from this position
     *
     * @param listener listener for intermediate results
     */
    public void calcHints(Position position, int maxDepth, int nHints, AbortCheck abortCheck, Listener listener) {
        final long moverMoves = position.calcMoves();
        if (moverMoves == 0) {
            throw new IllegalArgumentException("Must have a legal move to call calcHints()");
        }
        final long n0 = searcher.getCounts().nFlips;
        final long t0 = System.currentTimeMillis();
        final Depths depths = calcSearchDepth(position, maxDepth);

        // list of moveScores, sorted in descending order by score for at least the first nHints scores
        final ArrayList<MoveScore> moveScores = new ArrayList<>();
        long moves = position.calcMoves();
        while (moves != 0) {
            final int sq = Long.numberOfTrailingZeros(moves);
            moveScores.add(new MoveScore(sq, 0));
            moves &= moves - 1;
        }

        for (int i = 1; i <= depths.searchDepth; i++) {
            final Depth displayDepth = i < depths.searchDepth ? new Depth(i) : depths.displayDepth();
            listener.updateStatus("Searching at " + displayDepth.humanString());
            for (int j = 0; j < moveScores.size(); j++) {
                final int beta = 64 * CoefficientCalculator.DISK_VALUE;
                final int alpha = j < nHints ? -64 * CoefficientCalculator.DISK_VALUE : moveScores.get(nHints - 1).centidisks;
                final int sq = moveScores.get(j).sq;
                final int score = -searcher.calcScore(position.play(sq), alpha, beta, i - 1, abortCheck);
                final MoveScore moveScore = new MoveScore(sq, score);
                if (score > alpha || score == -64 * CoefficientCalculator.DISK_VALUE) {
                    insertSorted(moveScores, j, moveScore);
                    listener.hint(moveScore, displayDepth);
                } else {
                    moveScores.set(j, moveScore);
                }
                listener.updateNodeStats(searcher.getCounts().nFlips - n0, System.currentTimeMillis() - t0);
            }
        }

        if (shouldSolve(position, maxDepth)) {
            // full-width solve
            listener.updateStatus("Solving");
            final MoveScore moveScore = solver.getMoveScore(position.mover(), position.enemy(), abortCheck);
            listener.hint(moveScore, new Depth("100%"));
            listener.updateNodeStats(solver.getCounts().nFlips - n0, System.currentTimeMillis() - t0);
        }
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

    private boolean shouldSolve(Position position, int maxDepth) {
        return midgameOptions.useNtestSearchDepths && position.nEmpty() <= new Heights(maxDepth).getFullWidthHeight();
    }

    public interface Listener {
        Listener NULL = new Listener() {
            @Override public void updateStatus(String status) {
            }

            @Override public void updateNodeStats(long nodeCount, long millis) {
            }

            @Override public void hint(MoveScore moveScore, Depth depth) {
            }

            @Override public void analysis(int moveNumber, double eval) {
            }
        };

        void updateStatus(String status);

        void updateNodeStats(long nodeCount, long millis);

        void hint(MoveScore moveScore, Depth depth);

        /**
         * The engine is reporting the results of a retrograde analysis of a game.
         *
         * @param moveNumber number of moves that have been played in the game.
         * @param eval       evaluation; positive numbers mean black is ahead.
         */
        void analysis(int moveNumber, double eval);
    }
}

