package com.welty.novello.solver;

import static com.welty.novello.solver.BitBoardUtils.*;

/**
 * Calculates and sorts legal moves
 */
public final class MoveSorter {
    /**
     * fixed ordering value << FIXED_ORDERING_WEIGHT is added to the move sort score.
     */
    static final int FIXED_ORDERING_WEIGHT = 0;

    /**
     * mobility value << MOBILITY_WEIGHT is added to the move sort score
     */
    static final int MOBILITY_WEIGHT = 2;

    /**
     * 1 << ETC_WEIGHT is added to the move sort score if the position is in the hash table and will immediately cut off.
     */
    static int ETC_WEIGHT = 10;

    /**
     * If the move plays into an odd region, 1<< PARITY_WEIGHT is added to the score
     */
    static final int PARITY_WEIGHT = 0;

    /**
     * lookup table to get sort weight from mobility.
     * This allows for nonlinear parameters.
     */
    static int[] sortWeightFromMobility = new int[64];

    static {
        for (int i = 0; i < 64; i++) {
            sortWeightFromMobility[i] = (int) (20 * Math.log(i + 0.5));
        }
    }

    private int size = 0;
    final Move[] moves = new Move[64];

    MoveSorter() {
        for (int i = 0; i < moves.length; i++) {
            moves[i] = new Move();
        }
    }

    int size() {
        return size;
    }

    /**
     * Create a sorted list containing all legal moves for the mover that are listed in movesToCheck.
     * <p/>
     * This version uses ETC.
     * <p/>
     * if movesToCheck is -1L, all moves will be checked. Setting movesToCheck to the mobilities can improve the timing
     * since only known legal moves will have their flips calculated. However, the timing benefit is smaller than the cost
     * of calculating the mobilities, so this should only be set if mobilities are calculated for some other reason such
     * as move sorting from a higher depth.
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    public void createWithEtc(ListOfEmpties empties, long mover, long enemy, long parity, long movesToCheck
            , HashTable hashTable, int alpha, int beta) {
        size = 0;

        for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
            final Square square = node.square;
            if (isBitClear(movesToCheck, square.sq)) {
                continue;
            }

            final long flips = square.calcFlips(mover, enemy);
            if (flips != 0) {
                final long placement = 1L << square.sq;
                final long nextEnemy = mover | flips | placement;
                final long nextMover = enemy & ~flips;
                final long enemyMoves = calcMoves(nextMover, nextEnemy);
                final int nMobs = Long.bitCount(enemyMoves);
                int score = (FixedMoveOrdering.getValue(square.sq) << FIXED_ORDERING_WEIGHT)
                        + (BitBoardUtils.getBitAsInt(parity, square.sq) << PARITY_WEIGHT)
                        - (sortWeightFromMobility[nMobs] << MOBILITY_WEIGHT);
                final HashTable.Entry entry = hashTable.find(nextMover, nextEnemy);
                if (entry != null && entry.cutsOff(-beta, -alpha)) {
                    score += 1 << ETC_WEIGHT;
                }

                insert(square.sq, score, flips, enemyMoves, node);
            }
        }
    }

    /**
     * Create a sorted list containing all legal moves for the mover that are listed in movesToCheck.
     * <p/>
     * This version does not use ETC.
     * <p/>
     * if movesToCheck is -1L, all moves will be checked. Setting movesToCheck to the mobilities can improve the timing
     * since only known legal moves will have their flips calculated. However, the timing benefit is smaller than the cost
     * of calculating the mobilities, so this should only be set if mobilities are calculated for some other reason such
     * as move sorting from a higher depth.
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    public void createWithoutEtc(ListOfEmpties empties, long mover, long enemy, long parity, long movesToCheck) {
        size = 0;

        for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
            final Square square = node.square;
            if (isBitClear(movesToCheck, square.sq)) {
                continue;
            }

            final long flips = square.calcFlips(mover, enemy);
            if (flips != 0) {
                final long placement = 1L << square.sq;
                final long nextEnemy = mover | flips | placement;
                final long nextMover = enemy & ~flips;
                final long enemyMoves = calcMoves(nextMover, nextEnemy);
                final int nMobs = Long.bitCount(enemyMoves);
                int score = (FixedMoveOrdering.getValue(square.sq) << FIXED_ORDERING_WEIGHT)
                        + (BitBoardUtils.getBitAsInt(parity, square.sq) << PARITY_WEIGHT)
                        - (sortWeightFromMobility[nMobs] << MOBILITY_WEIGHT);
                insert(square.sq, score, flips, enemyMoves, node);
            }
        }
    }

    /**
     * Insert the move into this MoveSorter, keeping the moves sorted.
     */
    void insert(int sq, int score, long flips, long enemyMoves, ListOfEmpties.Node node) {
        final Move move = moves[size++];

        move.sq = sq;
        move.score = score;
        move.flips = flips;
        move.enemyMoves = enemyMoves;
        move.node = node;

        int i = size - 1;
        while (i > 0 && moves[i - 1].score < move.score) {
            moves[i] = moves[i - 1];
            i--;
        }
        moves[i] = move;

    }
}
