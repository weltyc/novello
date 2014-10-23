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

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Board;
import com.welty.novello.core.NovelloUtils;
import com.welty.novello.solver.Solver;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import static com.welty.novello.core.NovelloUtils.NO_MOVE;

/**
 */
public class MidgameHashTables {
    private static final Logger log = Logger.logger(MidgameHashTables.class);

    private final HashTable[] tables;

    // statistics
    private long nFinds = 0;
    private long nStores = 0;

    // rely on the caller to update these.
    private long nAlphaCuts = 0;
    private long nBetaCuts = 0;
    private long nPvCuts = 0;
    private long nUselessFind = 0;

    /**
     * @return search statistics on the hash table
     */
    public String stats() {
        return String.format("%,d finds and %,d stores. %,d / %,d / %,d alpha/beta/pv cuts. %,d useless finds."
                , nFinds, nStores, nAlphaCuts, nBetaCuts, nPvCuts, nUselessFind);
    }

    private static final AtomicInteger count = new AtomicInteger();

    /**
     * Create a HashTables
     */
    public MidgameHashTables() {
        tables = new HashTable[64];
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            int size = 1 << (nEmpty < 8 ? 10 : nEmpty < 12 ? 12 : 14);
            tables[nEmpty] = new HashTable(size);
        }
        log.info(String.format("ouch: %d  (%,d entries)", count.incrementAndGet(), nEntries()));
    }

    private long nEntries() {
        long nEntries = 0;
        for (HashTable table : tables) {
            nEntries += table.entries.length;
        }
        return nEntries;
    }

    /**
     * Find the hash entry for the position
     *
     * @return hash entry for the position, or null if there is no entry
     */
    public
    @Nullable Entry find(long mover, long enemy) {
        nFinds++;
        final Entry entry = getEntry(mover, enemy);
        return entry.matches(mover, enemy) ? entry : null;
    }

    private Entry getEntry(long mover, long enemy) {
        final int nEmpty = Long.bitCount(~(mover | enemy));
        final HashTable table = tables[nEmpty];
        return table.getEntry(mover, enemy);
    }

    /**
     * Clear all entries.
     * <p/>
     * Mostly useful to prevent cheating in benchmarks
     *
     * @param maxNEmpties max # of empties that will be cleared in the hash table
     */
    public void clear(int maxNEmpties) {
        for (int nEmpty = Solver.MIN_HASH_DEPTH; nEmpty <= maxNEmpties; nEmpty++) {
            tables[nEmpty].clear();
        }
    }

    /**
     * Store a search result in the hash
     *
     * @param mover    mover bitboard
     * @param enemy    enemy bitboard
     * @param alpha    original search alpha
     * @param beta     original search beta
     * @param depth    search depth that produced the result
     * @param bestMove suggested move, or -1 if no suggestion
     * @param result   search result
     */
    public void store(long mover, long enemy, int alpha, int beta, int depth, int bestMove, int result) {
        nStores++;
        final Entry entry = getEntry(mover, enemy);
        entry.update(mover, enemy, alpha, beta, depth, bestMove, result);

    }

    /**
     * Extract the PV from this HashTables.
     * <p/>
     * This function searches the bestMove, if available, for each node. If the resulting position
     * is stored in this HashTables with score = +/- score, the move is appended to the PV.
     *
     * @param board position at root of search
     * @param score score, from mover's point of view, in net disks.
     */
    public String extractPv(Board board, int score) {
        final Entry entry = find(board.mover(), board.enemy());
        final StringBuilder sb = new StringBuilder();
        if (entry != null && entry.getMin() == score && entry.getMax() == score) {
            appendPv(board, sb, -score);
        }
        return sb.toString();
    }

    void appendPv(Board board, StringBuilder sb, int score) {
        long moves = board.calcMoves();
        while (moves != 0) {
            int sq = Long.numberOfTrailingZeros(moves);
            long mask = 1L << sq;
            moves ^= mask;
            Board subBoard = board.play(sq);
            Entry subEntry = find(subBoard.mover(), subBoard.enemy());
            if (subEntry != null && subEntry.getMin()==score && subEntry.getMax()==score) {
                sb.append(BitBoardUtils.sqToLowerText(sq)).append("-");
                appendPv(subBoard, sb, -score);
                return;
            }
        }
    }

    public void updateBetaCut() {
        nBetaCuts++;
    }

    public void updateAlphaCut() {
        nAlphaCuts++;
    }

    public void updatePvCut() {
        nPvCuts++;
    }

    public void updateUselessFind() {
        nUselessFind++;
    }

    public static class Entry {
        long mover;
        long enemy;
        private int min;
        private int max;
        int depth;
        private int bestMove;

        Entry() {
            mover = enemy = -1L; // invalid position, so we won't get it by accident
            min = NO_MOVE;
            max = -NO_MOVE;
            depth = -1;
            bestMove = -1;
        }

        boolean matches(long mover, long enemy) {
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
        public void update(long mover, long enemy, int alpha, int beta, int depth, int bestMove, int result) {
            assert alpha < beta;

            final boolean matches = matches(mover, enemy);
            if (matches) {
                if (depth >= this.depth) {
                    if (bestMove >= 0) {
                        this.bestMove = bestMove;
                    }

                    if (depth > this.depth) {
                        overwriteScore(alpha, beta, depth, result);
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
                overwriteScore(alpha, beta, depth, result);
                this.bestMove = bestMove;
            }

            assert min <= max;
        }

        private void overwriteScore(int alpha, int beta, int depth, int result) {
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
        }

        public void clear() {
            mover = enemy = -1;
            depth = -1;
        }

        /**
         * @return true if the value in this node would cause an immediate return due to alpha or beta cutoff
         */
        public boolean cutsOff(int alpha, int beta) {
            return min >= beta || max <= alpha || min == max;
        }

        /**
         * Get minimum value.
         * <p/>
         * A previous search found that value(depth) >= getMin()
         *
         * @return minimum value
         */
        public int getMin() {
            return min;
        }

        /**
         * Get maximum value.
         * <p/>
         * A previous search found that value(depth) &le; getMax()
         *
         * @return maximum value
         */
        public int getMax() {
            return max;
        }

        public int getDepth() {
            return depth;
        }

        public int getBestMove() {
            return bestMove;
        }

        /**
         * @return true if the score is exact (min == max)
         */
        public boolean isExact() {
            return min == max;
        }
    }

    static class HashTable {
        Entry[] entries;

        public HashTable(int size) {
            entries = new Entry[size];
            for (int i = 0; i < entries.length; i++) {
                entries[i] = new Entry();
            }
        }

        /**
         * Get the Entry that this position can be stored in.
         * <p/>
         * The position might or might not already be in the returned Entry.
         *
         * @param mover mover bits
         * @param enemy enemy bits
         * @return the Entry
         */
        public Entry getEntry(long mover, long enemy) {
            final int hash = (entries.length - 1) & (int) NovelloUtils.hash(mover, enemy);
            return entries[hash];
        }

        public void clear() {
            for (Entry entry : entries) {
                entry.clear();
            }
        }
    }
}
