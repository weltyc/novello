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

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Board;
import com.welty.novello.core.NovelloUtils;
import com.welty.novello.solver.Solver;
import org.jetbrains.annotations.Nullable;

/**
 */
public class HashTables {
    private final HashTable[] tables;

    // statistics
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
        return String.format("%,d stores. %,d / %,d / %,d alpha/beta/pv cuts. %,d useless finds."
                , nStores, nAlphaCuts, nBetaCuts, nPvCuts, nUselessFind);
    }

    /**
     * Create a HashTables
     */
    public HashTables() {
        tables = new HashTable[64];
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            int size = 1 << (nEmpty < 8 ? 10 : nEmpty < 12 ? 12 : 14);
            tables[nEmpty] = new HashTable(size);
        }
    }

    public Entry getEntry(long mover, long enemy) {
        final int nEmpty = Long.bitCount(~(mover | enemy));
        final HashTable table = tables[nEmpty];
        return table.getEntry(mover, enemy);
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
        final StringBuilder sb = new StringBuilder();
        if (entryHasScore(board, score)) {
            appendPv(board, sb, -score);
        }
        return sb.toString();
    }

    private boolean entryHasScore(Board board, int score) {
        long mover = board.mover();
        long enemy = board.enemy();
        return getEntry(mover, enemy).hasScore(mover, enemy, score);
    }

    void appendPv(Board board, StringBuilder sb, int score) {
        long moves = board.calcMoves();
        while (moves != 0) {
            int sq = Long.numberOfTrailingZeros(moves);
            long mask = 1L << sq;
            moves ^= mask;
            Board subBoard = board.play(sq);
            if (entryHasScore(board, score)) {
                sb.append(BitBoardUtils.sqToLowerText(sq)).append("-");
                appendPv(subBoard, sb, -score);
                return;
            }
        }
    }

    /**
     * Clear all entries.
     * <p/>
     * Mostly useful to prevent cheating in benchmarks
     *
     * @param maxNEmpties max # of empties that will be cleared in the hash table
     */
    public void clear(int maxNEmpties) {
        maxNEmpties = Math.min(maxNEmpties, tables.length - 1);
        for (int nEmpty = Solver.MIN_HASH_DEPTH; nEmpty <= maxNEmpties; nEmpty++) {
            tables[nEmpty].clear();
        }
    }

    /**
     * Store a search result in the hash
     *
     * @param mover  mover bitboard
     * @param enemy  enemy bitboard
     * @param alpha  original search alpha
     * @param beta   original search beta
     * @param result search result
     */
    public void store(long mover, long enemy, int alpha, int beta, int result) {
        nStores++;
        final Entry entry = getEntry(mover, enemy);
        entry.update(mover, enemy, alpha, beta, result);

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
