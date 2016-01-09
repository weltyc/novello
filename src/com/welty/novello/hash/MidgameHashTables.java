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
import com.welty.novello.solver.BA;
import com.welty.novello.solver.Solver;

import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public class MidgameHashTables {
    private static final Logger log = Logger.logger(MidgameHashTables.class);

    private final HashTable[] tables;

    // statistics
    private long nStores = 0;

    // rely on the caller to update these.
    private long nAlphaCuts = 0;
    private long nBetaCuts = 0;
    private long nPvCuts = 0;
    private long nUselessFind = 0;

    /**
     * Check to see if a search result can be determined from the hash table
     *
     * @param mover mover bitboard
     * @param enemy enemy bitboard
     * @param depth search depth
     * @param alpha search alpha
     * @param beta  search beta
     * @param width mpc cut width
     * @return a BA containing the search result, or null if the search result can't be determined.
     */
    public BA checkForHashCutoff(long mover, long enemy, int depth, int alpha, int beta, int width) {
        return getEntry(mover, enemy).getBa(mover, enemy, depth, alpha, beta, width);
    }

    /**
     * @return search statistics on the hash table
     */
    public String stats() {
        return String.format("%,d stores. %,d / %,d / %,d alpha/beta/pv cuts. %,d useless finds."
                , nStores, nAlphaCuts, nBetaCuts, nPvCuts, nUselessFind);
    }

    private static final AtomicInteger count = new AtomicInteger();

    /**
     * Create a HashTables
     */
    public MidgameHashTables() {
        tables = new HashTable[64];
        for (int nEmpty = 0; nEmpty < 64; nEmpty++) {
            int lgSize = nEmpty < 8 ? 10 : nEmpty < 12 ? 12 : 14;
            lgSize -= 3; // drop hash size so NBoard doesn't take up too much RAM. May need a way to adjust this in future.
            int size = 1 << lgSize;
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
     * Get the Entry corresponding to a position.
     * <p/>
     * This returns the Entry that might contain the position;
     * there is no guarantee that position stored in the Entry is actually the desired position.
     *
     * Entry is used in multithreaded applications so usages may need to be synchronized;
     * see {@link MidgameEntry} for details.
     *
     * @return Entry that might contain the position.
     */
    public MidgameEntry getEntry(long mover, long enemy) {
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
     * @param width    search width that produced the result
     * @param bestMove suggested move, or -1 if no suggestion
     * @param result   search result
     */
    public void store(long mover, long enemy, int alpha, int beta, int depth, int width, int bestMove, int result) {
        nStores++;
        getEntry(mover, enemy).update(mover, enemy, alpha, beta, depth, width, bestMove, result);

    }

    /**
     * Extract the PV from this HashTables.
     * <p/>
     * This function searches the bestMove, if available, for each node. If the resulting position
     * is stored in this HashTables with score = +/- score, the move is appended to the PV.
     *
     * @param board position at root of search
     * @param score score, from mover's point of view, in net centi-disks.
     */
    public String extractPv(Board board, int score) {
        final StringBuilder sb = new StringBuilder();
        return findAndAppend(board, score, sb);
    }

    /**
     * Extract the PV from this HashTables.
     * <p/>
     * This function searches the bestMove, if available, for each node. If the resulting position
     * is stored in this HashTables with score = +/- score, the move is appended to the PV.
     *
     * @param board position at root of search
     * @param sq    square of first move
     * @param score score, from mover's point of view, in net disks.
     */
    public String extractPv(Board board, int score, int sq) {
        final StringBuilder sb = new StringBuilder();
        sb.append(BitBoardUtils.sqToLowerText(sq)).append("-");
        return findAndAppend(board.play(sq), -score, sb);
    }

    private String findAndAppend(Board board, int score, StringBuilder sb) {
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

    /**
     * @return square of the best move, if it's available, or -1 if it's not
     */
    public int getSuggestedMove(long mover, long enemy) {
        return getEntry(mover, enemy).getSuggestedMove(mover, enemy);
    }

    static class HashTable {
        MidgameEntry[] entries;

        public HashTable(int size) {
            entries = new MidgameEntry[size];
            for (int i = 0; i < entries.length; i++) {
                entries[i] = new MidgameEntry();
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
        public MidgameEntry getEntry(long mover, long enemy) {
            final int hash = (entries.length - 1) & (int) NovelloUtils.hash(mover, enemy);
            return entries[hash];
        }

        public void clear() {
            for (MidgameEntry entry : entries) {
                entry.clear();
            }
        }
    }
}
