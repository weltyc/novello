package com.welty.novello.book;

import com.welty.novello.core.Board;
import com.welty.novello.core.MoveScore;

/**
 * Class that does searches for book adds.
 *
 * Adders must be thread-safe.
*/
public interface Adder {
    /**
     * Calculate the best move and its score using a midgame search.
     *
     * The search is restricted to the given moves.
     *
     * @param board position.
     * @param moves bitboard containing moves. Must not be 0.
     * @return Move and score of best move from moves.
     */
    public MoveScore calcDeviation(Board board, long moves);

    /**
     * Solve a position.
     * @param board position.
     * @return Move and score of best move from moves.
     */
    public MoveScore solve(Board board);

    /**
     * Get the maximum solve depth.
     *
     * This is the Maximum # of empties for which the Adder will solve. At higher numbers of empties it
     * will do a probable solve or midgame search.
     *
     * @return maximum number of empties for solve.
     */
    int solveDepth();
}
