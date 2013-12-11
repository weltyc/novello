package com.welty.novello.solver;

import com.welty.novello.core.NovelloUtils;
import org.jetbrains.annotations.Nullable;

/**
 */
public class HashTables {
    private final HashTable[] tables;

    // statistics
    private long nFinds = 0;
    private long nStores = 0;

    // rely on the caller to update these.
    long nAlphaCuts = 0;
    long nBetaCuts = 0;
    long nPvCuts = 0;
    long nUselessFind = 0;

    /**
     * @return search statistics on the hash table
     */
    public String stats() {
        return String.format("%,d finds and %,d stores. %,d / %,d / %,d alpha/beta/pv cuts. %,d useless finds."
                , nFinds, nStores, nAlphaCuts, nBetaCuts, nPvCuts, nUselessFind);
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

    static class Entry {
        long mover;
        long enemy;
        int min;
        int max;

        Entry() {
            mover = enemy = -1L; // invalid position, so we won't get it by accident
            min = -64;
            max = 64;
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
        public void update(long mover, long enemy, int alpha, int beta, int result) {
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

        public void clear() {
            mover = enemy = -1;
        }

        /**
         * @return true if the value in this node would cause an immediate return due to alpha or beta cutoff
         */
        public boolean cutsOff(int alpha, int beta) {
            return min >= beta || max <= alpha || min == max;
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
         *
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
