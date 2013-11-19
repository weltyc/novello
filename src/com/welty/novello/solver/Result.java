package com.welty.novello.solver;

/**
 * An immutable class that contains the result of a search
 */
public class Result {
    /**
     * Net score to mover
     */
    public final int score;

    /**
     * Index of best move in the search. For instance the first move searched is 0, the second move searched is 1, etc.
     * The 'best move' is the last move that increased alpha, or -1 if no move increased alpha.
     */
    public final int bestMoveSq;

    Result(int score, int bestMoveSq) {
        this.score = score;
        this.bestMoveSq = bestMoveSq;
    }
}
