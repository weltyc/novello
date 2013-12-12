package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Square;
import com.welty.novello.eval.CoefficientCalculator;
import org.jetbrains.annotations.NotNull;

import static com.welty.novello.core.BitBoardUtils.calcMoves;
import static com.welty.novello.core.BitBoardUtils.isBitClear;
import static com.welty.novello.eval.CoefficientCalculator.DISK_VALUE;
import static java.lang.Long.bitCount;

/**
 * Calculates and sorts legal moves
 */
final class MoveSorter {
    /**
     * fixed ordering value << FIXED_ORDERING_WEIGHT is added to the move sort score.
     */
    private static final int FIXED_ORDERING_WEIGHT = 1;

    /**
     * mobility value << MOBILITY_WEIGHT is added to the move sort score
     */
    private static final int MOBILITY_WEIGHT = 4;

    /**
     * mobility value << DEEP_MOBILITY_WEIGHT is added to the move sort score when using eval
     */
    private static final int DEEP_MOBILITY_WEIGHT = 4;

    /**
     * 1 << ETC_WEIGHT is added to the move sort score if the position is in the hash table and will immediately cut off.
     */
    private static final int ETC_WEIGHT = 11;

    /**
     * If the move plays into an odd region, 1<< PARITY_WEIGHT is added to the score
     */
    private static final int PARITY_WEIGHT = 1;

    /**
     * net potential mobility << POT_MOB_WEIGHT is added to the move sort score
     */
    private static final int MOVER_POT_MOB_WEIGHT = 0;
    private static final int ENEMY_POT_MOB_WEIGHT = 4;
    /**
     * Only check for ETC at this depth or higher.
     * <p/>
     * if depth is Solver.MIN_HASH_DEPTH or below, the children will never be in hash.
     */
    private static final int MIN_ETC_DEPTH = Solver.MIN_HASH_DEPTH + 1;
    static int DEEP_POT_MOB_WEIGHT = 4;

    private static final int BETA_MARGIN = 24;

    /**
     * lookup table to get sort weight from mobility.
     * This allows for nonlinear parameters.
     */
    private static final int[] sortWeightFromMobility = new int[64];

    static {
        for (int i = 0; i < 64; i++) {
            sortWeightFromMobility[i] = ((int) (20 * Math.log(i + 0.5))) << MOBILITY_WEIGHT;
        }
    }

    /**
     * Use the eval for sorting at this depth and higher.
     */
    static int MIN_EVAL_SORT_DEPTH = 12;

    private int size = 0;

    final SorterMove[] sorterMoves = new SorterMove[64];

    private final @NotNull Counter counter;
    private final @NotNull Search sortSearch;

    MoveSorter(@NotNull Counter counter) {
        this.counter = counter;
        for (int i = 0; i < sorterMoves.length; i++) {
            sorterMoves[i] = new SorterMove();
        }
        sortSearch = new Search(counter, 0);
    }

    static final int[][] searchDepths = {
            {-1, 0, 0, 0, -1, 2}, // 12-13
            {-1, 0, 0, 0, 0, 1},  // 14-15
            {-1, 0, 2, 1, 0, 4},  //16-17
            {0, 2, 2, 2, 0, 3}, // 18-19
            {0, 2, 3, 4, 2, 4}, // 20-21
            {2, 2, 4, 4, 2, 5}, // 22-23
            {2, 2, 2, 2, 2, 6}, // 24-25
            {2, 2, 2, 2, 2, 7}, // 26-27
            {2, 2, 2, 2, 2, 8}, // 28-29
            {2, 2, 2, 2, 2, 9}, // 30-31
            {2, 2, 2, 2, 2, 10}, // 32-33
            {2, 2, 2, 2, 2, 11}, // 34-35
    };

    static final int[][] fastestFirsts = {
            {4,3,3,3,4,3},
            {4,3,3,3,3,3},
            {5,3,2,3,3,2},
            {4,1,2,3,3,2},
            {2,1,1,3,3,3},
            {0,3,2,2,2,2},
            {2,2,2,2,2,2},
            {2,2,2,2,2,2},
            {2,2,2,2,2,2},
            {2,2,2,2,2,2},
            {2,2,2,2,2,2},
            {2,2,2,2,2,2},
    };

    /**
     * Choose a move sorting method appropriate for the position and sort the moves.
     */
    void createSort(long mover, long enemy, int alpha, int beta, int nEmpties, long parity, long movesToCheck
            , ListOfEmpties empties, HashTables hashTables, int predictedNodeType) {
        if (nEmpties >= MIN_ETC_DEPTH) {
            final int searchDepth; // search depth for createWithEtcAndEval, or -1 to use createWithEtc.
            final int fastestFirstWeight;
            if (nEmpties < MIN_EVAL_SORT_DEPTH) {
                searchDepth = -1;
                fastestFirstWeight = 0; // currently ignored when searchDepth = -1.
            } else {
                final int emptyBucket = (nEmpties - MIN_EVAL_SORT_DEPTH)/2;
                final int evalBucket = calcEvalBucket(mover, enemy, alpha, beta, predictedNodeType);
                searchDepth = searchDepths[emptyBucket][evalBucket];
                fastestFirstWeight = fastestFirsts[emptyBucket][evalBucket];
            }
            if (searchDepth >= 0) {
                createWithEtcAndEval(empties, mover, enemy, movesToCheck, hashTables, alpha, beta, searchDepth, fastestFirstWeight);
            } else {
                createWithEtc(empties, mover, enemy, parity, movesToCheck, hashTables, alpha, beta);
            }
        } else {
            createWithoutEtc(empties, mover, enemy, parity, movesToCheck);
        }
    }

    /**
     * Put the position into one of five buckets, depending on its eval vs the (alpha, beta) range
     *
     * @return bucket number, 0..4
     */
    private int calcEvalBucket(long mover, long enemy, int alpha, int beta, int predictedNodeType) {
        int localScore = counter.eval(mover, enemy);
        if (predictedNodeType == Solver.PRED_ALL) {
            localScore -= 10;
        }
        final int evalBucket;
        if (predictedNodeType == Solver.PRED_PV) {
            evalBucket = 5;
        }
        else if (localScore > beta + 20* DISK_VALUE) {
            evalBucket = 4;
        }
        else if (localScore > beta + 7*DISK_VALUE) {
            evalBucket = 3;
        }
        else if (localScore >= alpha - 7*DISK_VALUE) {
            evalBucket = 2;
        }
        else if (localScore >= alpha - 20*DISK_VALUE) {
            evalBucket = 1;
        }
        else {
            evalBucket = 0;
        }
        return evalBucket;
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
    private void createWithEtcAndEval(ListOfEmpties empties, long mover, long enemy, long movesToCheck
            , HashTables hashTables, int alpha, int beta, int searchDepth, int fastestFirstWeight) {
        size = 0;

        for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
            final Square square = node.square;
            final int sq = square.sq;
            if (isBitClear(movesToCheck, sq)) {
                continue;
            }

            final long flips = counter.calcFlips(square, mover, enemy);
            if (flips != 0) {
                final long placement = 1L << sq;
                final long nextEnemy = mover | flips | placement;
                final long nextMover = enemy & ~flips;
                final long enemyMoves = calcMoves(nextMover, nextEnemy);
                int score = scoreWithEtcAndEval(hashTables, alpha, beta, nextEnemy, nextMover, enemyMoves, searchDepth, fastestFirstWeight);

                insert(sq, score, flips, enemyMoves, node);
            }
        }
    }


    private int scoreWithEtcAndEval(HashTables hashTables, int alpha, int beta, long nextEnemy, long nextMover
            , long nextMoverMoves, int searchDepth, int fastestFirstWeight) {
        final int nMobs = Long.bitCount(nextMoverMoves);

        final int evalScore;
        evalScore = sortSearch.calcScore(nextMover, nextEnemy, searchDepth, true);
//        final long dFlips = sortSearch.nFlips();

        int margin = -evalScore - (beta + BETA_MARGIN) * DISK_VALUE;
        if (margin > 0) {
            margin >>= 1;
            // doesn't help very much
//            final int nStable = Long.bitCount(Stable.stable(nextMover, nextEnemy) & nextEnemy);
//            margin += nStable * (CoefficientCalculator.DISK_VALUE/2);
        }

        final int costPenalty = sortWeightFromMobility[nMobs] << fastestFirstWeight;
        int moverPotMob = Long.bitCount(BitBoardUtils.potMobs2(nextEnemy, ~(nextMover | nextEnemy)));
        int score = margin - costPenalty - (moverPotMob << DEEP_POT_MOB_WEIGHT);
        final HashTables.Entry entry = hashTables.find(nextMover, nextEnemy);
        if (entry != null && entry.cutsOff(-beta, -alpha)) {
            score += 1 << ETC_WEIGHT;
        }
        return score;
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
    private void createWithEtc(ListOfEmpties empties, long mover, long enemy, long parity, long movesToCheck
            , HashTables hashTables, int alpha, int beta) {
        size = 0;

        for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
            final Square square = node.square;
            final int sq = square.sq;
            if (isBitClear(movesToCheck, sq)) {
                continue;
            }

            final long flips = counter.calcFlips(square, mover, enemy);
            if (flips != 0) {
                final long placement = 1L << sq;
                final long nextEnemy = mover | flips | placement;
                final long nextMover = enemy & ~flips;
                final long nextMoverMoves = calcMoves(nextMover, nextEnemy);
                int score = scoreWithEtc(parity, hashTables, alpha, beta, sq, nextEnemy, nextMover, nextMoverMoves);

                insert(sq, score, flips, nextMoverMoves, node);
            }
        }
    }

    private static int scoreWithEtc(long parity, HashTables hashTables, int alpha, int beta, int sq, long nextEnemy
            , long nextMover, long nextMoverMoves) {
        int score = eval(parity, sq, nextMoverMoves, nextMover, nextEnemy);
        final HashTables.Entry entry = hashTables.find(nextMover, nextEnemy);
        if (entry != null && entry.cutsOff(-beta, -alpha)) {
            score += 1 << ETC_WEIGHT;
        }
        return score;
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    private static int eval(long parity, int sq, long nextMoverMoves, long nextMover, long nextEnemy) {
        final long empty = ~(nextMover | nextEnemy);
        final int nMobs = bitCount(nextMoverMoves);
        return (FixedMoveOrdering.getValue(sq) << FIXED_ORDERING_WEIGHT)
                + (BitBoardUtils.getBitAsInt(parity, sq) << PARITY_WEIGHT)
                - (nPotMobs(nextEnemy, empty) << ENEMY_POT_MOB_WEIGHT)
                + (nPotMobs(nextMover, empty) << MOVER_POT_MOB_WEIGHT)
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
    private void createWithoutEtc(ListOfEmpties empties, long mover, long enemy, long parity, long movesToCheck) {
        size = 0;

        for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
            final Square square = node.square;
            if (isBitClear(movesToCheck, square.sq)) {
                continue;
            }

            final long flips = counter.calcFlips(square, mover, enemy);
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
        final SorterMove sorterMove = sorterMoves[size++];

        sorterMove.sq = sq;
        sorterMove.score = score;
        sorterMove.flips = flips;
        sorterMove.enemyMoves = enemyMoves;
        sorterMove.node = node;

        int i = size - 1;
        while (i > 0 && sorterMoves[i - 1].score < sorterMove.score) {
            sorterMoves[i] = sorterMoves[i - 1];
            i--;
        }
        sorterMoves[i] = sorterMove;

    }

    /**
     * Get square of the best move (after sort is complete)
     *
     * @param iBestMove index of the best move
     * @return square of the best move
     */
    public int sq(int iBestMove) {
        return sorterMoves[iBestMove].sq;
    }
}
