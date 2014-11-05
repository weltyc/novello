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

public class Entry {
    private long mover;
    private long enemy;
    private int min;
    private int max;

    Entry() {
        mover = enemy = -1L; // invalid position, so we won't get it by accident
        min = -64;
        max = 64;
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
     * WARNING: Not thread safe. You must use this with a synchronized Entry.
     *
     * Just synchronizing this method is not enough, since the caller will always want to do something
     * if the Entry matches.
     *
     * @param mover
     * @param enemy
     * @return true if this entry contains data for the given position.
     */
    public boolean matches(long mover, long enemy) {
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
     * @param mover  mover bitboard
     * @param enemy  enemy bitboard
     * @param alpha  original search alpha
     * @param beta   original search beta
     * @param result search result
     */
    synchronized  void update(long mover, long enemy, int alpha, int beta, int result) {
        if (matches(mover, enemy)) {
            if (result >= beta) {
                if (result > min) {
                    min = result;
                }
            } else if (result <= alpha) {
                if (result < max) {
                    max = result;
                }
            } else {
                max = min = result;
            }
        } else {
            // overwrite
            this.mover = mover;
            this.enemy = enemy;
            if (result >= beta) {
                min = result;
                max = 64;
            } else if (result <= alpha) {
                max = result;
                min = -64;
            } else {
                max = min = result;
            }
        }
    }

    synchronized void clear() {
        mover = enemy = -1;
    }

    /**
     * @return true if the value in this node would cause an immediate return due to alpha or beta cutoff
     */
    public synchronized boolean cutsOff(long mover, long enemy, int alpha, int beta) {
        return matches(mover, enemy) && (min >= beta || max <= alpha || min == max);
    }

    public synchronized int getMin() {
        return min;
    }

    public synchronized int getMax() {
        return max;
    }
}
