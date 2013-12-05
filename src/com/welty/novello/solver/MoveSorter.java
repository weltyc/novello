package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Square;
import com.welty.novello.eval.CoefficientCalculator;
import com.welty.novello.eval.Eval;
import com.welty.novello.selfplay.EvalPlayer;
import com.welty.novello.selfplay.Players;

import static com.welty.novello.core.BitBoardUtils.calcMoves;
import static com.welty.novello.core.BitBoardUtils.isBitClear;
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
    final Move[] moves = new Move[64];

    static final Eval deepEval = Players.currentEval();

    static final EvalPlayer deepEvalPlayer = new EvalPlayer(deepEval, 2);

    MoveSorter() {
        for (int i = 0; i < moves.length; i++) {
            moves[i] = new Move();
        }
    }

    /**
     * Choose a move sorting method appropriate for the position and sort the moves.
     */
    void createSort(long mover, long enemy, int alpha, int beta, int nEmpties, long parity, long movesToCheck
            , ListOfEmpties empties, HashTables hashTables, int predictedNodeType) {
        if (nEmpties >= MIN_ETC_DEPTH) {
            final boolean useEvalSort;
            if (nEmpties < MIN_EVAL_SORT_DEPTH) {
                useEvalSort = false;
            } else if (nEmpties >= MIN_EVAL_SORT_DEPTH + 2) {
                useEvalSort = true;
            } else {
                int localScore = deepEval.eval(mover, enemy);
                if (predictedNodeType==Solver.PRED_ALL) {
                    localScore -= 10;
                }
                useEvalSort = localScore > (alpha - 20) * CoefficientCalculator.DISK_VALUE;
            }
            if (useEvalSort) {
                final int nEmpty = Long.bitCount(~(mover|enemy));
                final int searchDepth;
                if (nEmpty >= 16) {
                    // decide if we should use an eval or a 1-ply search for valuing the subnodes.
                    // 1-ply search is better but expensive; use it only if it's going to help.
                    //
                    // We guess whether it will help by looking at the current eval; if it's close to the range
                    // (alpha, beta) it's likely to help.
                    //
                    // The current eval has a small adjustment for the current predicted node type; it turns out
                    // that predicted ALL nodes cut off less frequently than predicted CUT nodes with the same eval.
                    int localScore = deepEval.eval(mover, enemy);
                    if (predictedNodeType==Solver.PRED_ALL) {
                        localScore -= 10;
                    }
                    searchDepth = nEmpty >= 19 || scoreInRange(localScore, alpha-20, beta+20) ? 1 : 0;
                }
                else {
                    searchDepth = 0;
                }
                createWithEtcAndEval(empties, mover, enemy, movesToCheck, hashTables, alpha, beta, searchDepth);
            } else {
                createWithEtc(empties, mover, enemy, parity, movesToCheck, hashTables, alpha, beta);
            }
        } else {
            createWithoutEtc(empties, mover, enemy, parity, movesToCheck);
        }
    }

    private boolean scoreInRange(int localScore, int min, int max) {
        return localScore >= min*CoefficientCalculator.DISK_VALUE && localScore <= max*CoefficientCalculator.DISK_VALUE;
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
            , HashTables hashTables, int alpha, int beta, int searchDepth) {
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
                int score = scoreWithEtcAndEval(hashTables, alpha, beta, nextEnemy, nextMover, enemyMoves, searchDepth);

                insert(sq, score, flips, enemyMoves, node);
            }
        }
    }


    private static int scoreWithEtcAndEval(HashTables hashTables, int alpha, int beta, long nextEnemy, long nextMover, long nextMoverMoves, int searchDepth) {
        final int nMobs = Long.bitCount(nextMoverMoves);

        final int evalScore;
        if (searchDepth > 0) {
            evalScore = deepEvalPlayer.searchScore(nextMover, nextEnemy, -Integer.MAX_VALUE, Integer.MAX_VALUE, searchDepth);
        }
        else {
            evalScore = deepEval.eval(nextMover, nextEnemy, nextMoverMoves);
        }
        int margin = -evalScore - (beta + BETA_MARGIN) * CoefficientCalculator.DISK_VALUE;
        if (margin > 0) {
            margin >>= 1;
        }
        int moverPotMob = Long.bitCount(BitBoardUtils.potMobs2(nextEnemy, ~(nextMover | nextEnemy)));
        int score = margin - (sortWeightFromMobility[nMobs] << DEEP_MOBILITY_WEIGHT)-(moverPotMob << DEEP_POT_MOB_WEIGHT);
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

            final long flips = square.calcFlips(mover, enemy);
            if (flips != 0) {
                final long placement = 1L << sq;
                final long nextEnemy = mover | flips | placement;
                final long nextMover = enemy & ~flips;
                final long enemyMoves = calcMoves(nextMover, nextEnemy);
                int score = scoreWithEtc(parity, hashTables, alpha, beta, sq, nextEnemy, nextMover, enemyMoves);

                insert(sq, score, flips, enemyMoves, node);
            }
        }
    }

    private static int scoreWithEtc(long parity, HashTables hashTables, int alpha, int beta, int sq, long nextEnemy, long nextMover, long enemyMoves) {
        int score = eval(parity, sq, enemyMoves, nextMover, nextEnemy);
        final HashTables.Entry entry = hashTables.find(nextMover, nextEnemy);
        if (entry != null && entry.cutsOff(-beta, -alpha)) {
            score += 1 << ETC_WEIGHT;
        }
        return score;
    }

    @SuppressWarnings("PointlessBitwiseExpression")
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
    private void createWithoutEtc(ListOfEmpties empties, long mover, long enemy, long parity, long movesToCheck) {
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
