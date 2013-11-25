package com.welty.novello.solver;

import static com.welty.novello.solver.BitBoardUtils.*;
import static java.lang.Long.bitCount;

/**
 * Calculates and sorts legal moves
 */
public final class MoveSorter {
    /**
     * fixed ordering value << FIXED_ORDERING_WEIGHT is added to the move sort score.
     */
    static final int FIXED_ORDERING_WEIGHT = 1;

    /**
     * mobility value << MOBILITY_WEIGHT is added to the move sort score
     */
    static final int MOBILITY_WEIGHT = 4;

    /**
     * 1 << ETC_WEIGHT is added to the move sort score if the position is in the hash table and will immediately cut off.
     */
    static final int ETC_WEIGHT = 11;

    /**
     * If the move plays into an odd region, 1<< PARITY_WEIGHT is added to the score
     */
    static final int PARITY_WEIGHT = 1;

    /**
     * net potential mobility << POT_MOB_WEIGHT is added to the move sort score
     */
    static final int MOVER_POT_MOB_WEIGHT = 0;
    static final int ENEMY_POT_MOB_WEIGHT = 4;

    /**
     * lookup table to get sort weight from mobility.
     * This allows for nonlinear parameters.
     */
    static int[] sortWeightFromMobility = new int[64];

    static {
        for (int i = 0; i < 64; i++) {
            sortWeightFromMobility[i] = ((int) (20 * Math.log(i + 0.5))) << MOBILITY_WEIGHT;
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
            final int sq = square.sq;
            if (isBitClear(movesToCheck, sq)) {
                continue;
            }

            final long flips = square.calcFlips(mover, enemy);
            if (flips != 0) {
                final long placement = 1L << sq;
                final long nextEnemy = mover | flips | placement;
                final long nextMover = enemy & ~flips;
                final long enemyMoves = calcMoves(nextMover, nextEnemy);
                int score = scoreWithEtc(parity, hashTable, alpha, beta, sq, nextEnemy, nextMover, enemyMoves);

                insert(sq, score, flips, enemyMoves, node);
            }
        }
    }

    private static int scoreWithEtc(long parity, HashTable hashTable, int alpha, int beta, int sq, long nextEnemy, long nextMover, long enemyMoves) {
        int score = eval(parity, sq, enemyMoves, nextMover, nextEnemy);
        final HashTable.Entry entry = hashTable.find(nextMover, nextEnemy);
        if (entry != null && entry.cutsOff(-beta, -alpha)) {
            score += 1 << ETC_WEIGHT;
        }
        return score;
    }

    private static int eval(long parity, int sq, long enemyMoves, long mover, long enemy) {
        final long empty = ~(mover | enemy);
        final int nMobs = bitCount(enemyMoves);
        return (FixedMoveOrdering.getValue(sq) << FIXED_ORDERING_WEIGHT)
                + (BitBoardUtils.getBitAsInt(parity, sq) << PARITY_WEIGHT)
                - (nPotMobs(enemy, empty) << ENEMY_POT_MOB_WEIGHT)
                + (nPotMobs(mover, empty) << MOVER_POT_MOB_WEIGHT)
                - sortWeightFromMobility[nMobs];
    }

    private static int nPotMobs(long player, long empty) {
        return bitCount(BitBoardUtils.potMobs(player, empty));
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
                int score = scoreWithoutEtc(parity, square, nextEnemy, nextMover, enemyMoves);
                insert(square.sq, score, flips, enemyMoves, node);
            }
        }
    }

    private static int scoreWithoutEtc(long parity, Square square, long nextEnemy, long nextMover, long enemyMoves) {
        return eval(parity, square.sq, enemyMoves, nextMover, nextEnemy);
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
