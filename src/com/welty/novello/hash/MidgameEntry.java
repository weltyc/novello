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

package com.welty.novello.hash;

import com.welty.novello.solver.BA;

import static com.welty.novello.core.NovelloUtils.NO_MOVE;

/**
 * Midgame has table entry.
 * <p/>
 * Entry is thread-safe, meaning all non-private functions are synchronized.
 *
 * If callers need to ensure multiple accesses to the same Entry are consistent they
 * will need to synchronize on the Entry themselves.
 */
public class MidgameEntry {
    private long mover;
    private long enemy;
    private int min;
    private int max;

    /**
     * Depth, in ply of the stored search.
     */
    private int depth;

    /**
     * index of width into MPC cut widths for the stored search.
     * <p/>
     * Higher widths are more precise. A stored result can't be used if it is narrower than the search.
     */
    private int width;

    /**
     * Suggested move for further plies, or -1 if there is no suggested move
     */
    private int bestMove;

    MidgameEntry() {
        mover = enemy = -1L; // invalid position, so we won't get it by accident
        min = NO_MOVE;
        max = -NO_MOVE;
        depth = -1;
        width = -1;
        bestMove = -1;
    }

    /**
     * @return square of the best move, if it's available, or -1 if it's not
     */
    synchronized int getSuggestedMove(long mover, long enemy) {
        if (matches(mover, enemy)) {
            return bestMove;
        } else {
            return -1;
        }
    }

    /**
     * Check to see if a search result can be determined from the hash table
     *
     * @return a BA containing the search result, or null if the search result can't be determined.
     */
    synchronized BA getBa(long mover, long enemy, int depth, int alpha, int beta, int width) {
        if (matches(mover, enemy) && deepEnoughToSearch(depth, width)) {
            if (min >= beta) {
                final BA ba = new BA();
                ba.bestMove = bestMove;
                ba.score = min;
                return ba;
            }
            if (max <= alpha || isExact()) {
                final BA ba = new BA();
                ba.bestMove = bestMove;
                ba.score = max;
                return ba;
            }
        }
        return null;
    }

    /**
     * Does this position have a given exact score?
     *
     * @return true if this Entry contains the position and has the given score.
     */
    synchronized boolean hasScore(long mover, long enemy, int score) {
        return matches(mover, enemy) && min == score && max == score;
    }

    /**
     * Does this Entry contain the given position (at any depth)?
     *
     * @param mover mover bitboard
     * @param enemy enemy bitboard
     * @return true if this Entry contains the position.
     */
    synchronized boolean matches(long mover, long enemy) {
        return this.mover == mover && this.enemy == enemy;
    }

    /**
     * Update the entry.
     * <p/>
     * Always overwrites existing positions.
     * <p/>
     * When overwriting, the move is always stored.
     * When updating, the move is stored only if the min is increased.
     *
     * @param mover       mover bitboard
     * @param enemy       enemy bitboard
     * @param alpha       original search alpha
     * @param beta        original search beta
     * @param searchDepth search depth, in ply
     * @param searchWidth search width index
     * @param result      search result
     */
    synchronized void update(long mover, long enemy, int alpha, int beta, int searchDepth, int searchWidth, int bestMove, int result) {
        assert alpha < beta;

        final boolean matches = matches(mover, enemy);
        if (matches) {
            if (deepEnoughToStore(searchDepth, searchWidth)) {
                if (bestMove >= 0) {
                    this.bestMove = bestMove;
                }

                // The depth and width are deep enough. If the depth or width doesn't match, that means the search
                // is deeper than this Entry and we need to overwrite.
                if (depth != searchDepth || width != searchWidth) {
                    overwriteScore(alpha, beta, searchDepth, searchWidth, result);
                } else {
                    // depth == this.depth
                    if (result >= beta) {
                        if (result > min) {
                            min = result;
                            if (result > max) {
                                max = result;
                            }
                        }
                    } else if (result <= alpha) {
                        if (result < max) {
                            max = result;
                            if (result < min) {
                                min = result;
                            }
                        }
                    } else {
                        max = min = result;
                    }
                }
            }
        } else {
            // new position. overwrite.
            this.mover = mover;
            this.enemy = enemy;
            overwriteScore(alpha, beta, searchDepth, searchWidth, result);
            this.bestMove = bestMove;
        }

        assert min <= max;
    }

    private void overwriteScore(int alpha, int beta, int depth, int width, int result) {
        if (result >= beta) {
            min = result;
            max = -NO_MOVE;
        } else if (result <= alpha) {
            max = result;
            min = NO_MOVE;
        } else {
            max = min = result;
        }
        this.depth = depth;
        this.width = width;
    }

    synchronized void clear() {
        mover = enemy = -1;
        depth = -1;
    }

    /**
     * Get minimum value.
     * <p/>
     * A previous search found that value(depth) >= getMin()
     *
     * @return minimum value
     */
    synchronized int getMin() {
        return min;
    }

    /**
     * Get maximum value.
     * <p/>
     * A previous search found that value(depth) &le; getMax()
     *
     * @return maximum value
     */
    synchronized int getMax() {
        return max;
    }

    /**
     * Get search depth.
     * <p/>
     * For testing only. Otherwise use deepEnoughToSearch().
     *
     * @return search depth, in ply
     */
    synchronized int getDepth() {
        return depth;
    }

    /**
     * Is this entry deep enough to be used as a result for the current search?
     *
     * @param searchDepth depth of current search
     * @param searchWidth depth of current width
     * @return true if deep enough
     */
    synchronized boolean deepEnoughToSearch(int searchDepth, int searchWidth) {
        return depth >= searchDepth && width >= searchWidth;
    }

    /**
     * Is a search deep enough to store its value?
     *
     * @param searchDepth depth of current search
     * @param searchWidth depth of current width
     * @return true if deep enough
     */
    private boolean deepEnoughToStore(int searchDepth, int searchWidth) {
        return depth <= searchDepth && width <= searchWidth;
    }

    /**
     * @return suggested move for further plies, or -1 if there is no suggested move
     */
    synchronized int getBestMove() {
        return bestMove;
    }

    /**
     * @return true if the score is exact (min == max)
     */
    private boolean isExact() {
        return min == max;
    }
}
