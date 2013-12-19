package com.welty.novello.solver;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Square;

import static java.lang.Long.bitCount;

public class ShallowSolver {
    static final int NO_MOVE = -65;
    /**
     * At this depth and above, the search will check moves into odd parity regions
     * before moves into even parity regions
     */
    static final int MIN_PARITY_DEPTH = 5;

    static ListOfEmpties createEmptiesList(long mover, long enemy) {
        long empties = ~(mover | enemy);
        final ListOfEmpties emptySquares = new ListOfEmpties();
        for (long mask : FixedMoveOrdering.masks) {
            populateUnsorted(emptySquares, empties & mask);
        }
        return emptySquares;
    }

    private static void populateUnsorted(ListOfEmpties emptySquares, long empties) {
        while (empties != 0) {
            final int sq = Long.numberOfTrailingZeros(empties);
            final Square square = Square.of(sq);
            empties ^= square.placement();
            emptySquares.add(square);
        }
    }

    /**
     * Solve a position.
     *
     * @return position value to mover, in disks
     */
    public static int solveNoParity(Counter counter, long mover, long enemy, int alpha, int beta, int nEmpties, long movesToCheck) {
        final ListOfEmpties emptiesList = createEmptiesList(mover, enemy);
        if (nEmpties < MIN_PARITY_DEPTH) {
            return solveNoParity(counter, mover, enemy, alpha, beta, emptiesList, nEmpties);
        } else {
            return solveNoSort(counter, mover, enemy, alpha, beta, emptiesList, nEmpties, emptiesList.calcParity(), movesToCheck);
        }
    }

    static int solveNoParity(Counter counter, long mover, long enemy, int alpha, int beta, ListOfEmpties empties, int nEmpties) {
        if (nEmpties == 3) {
            return solve3(counter, mover, enemy, alpha, beta, empties);
        }
        final int result = moverResultNoParity(counter, empties, mover, enemy, alpha, beta, nEmpties);
        if (result == NO_MOVE) {
            final int enemyResult = moverResultNoParity(counter, empties, enemy, mover, -beta, -alpha, nEmpties);
            if (enemyResult == NO_MOVE) {
                return BitBoardUtils.terminalScore(mover, enemy);
            } else {
                return -enemyResult;
            }
        } else {
            return result;
        }
    }

    /**
     * alpha, beta, result from mover's point of view.
     *
     * @return solve value according to fail-soft alpha/beta, unless there are no legal moves in which case it returns NO_MOVE.
     */
    private static int moverResultNoParity(Counter counter, ListOfEmpties empties, long mover, long enemy, int alpha, int beta, int nEmpties) {
        int result = NO_MOVE;
        for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
            final Square square = node.square;
            final long flips = counter.calcFlips(square, mover, enemy);
            if (flips != 0) {
                final long subMover = enemy & ~flips;
                final long subEnemy = mover | flips | square.placement();
                node.remove();
                final int subResult = -solveNoParity(counter, subMover, subEnemy, -beta, -alpha, empties, nEmpties - 1);
                node.restore();
                if (subResult > result) {
                    result = subResult;
                    if (subResult > alpha) {
                        if (subResult >= beta) {
                            return result;
                        }
                        alpha = subResult;
                    }
                }
            }
        }
        return result;
    }

    private static int solve3(Counter counter, long mover, long enemy, int alpha, int beta, ListOfEmpties empties) {
        final int result = moverResult3(counter, empties, mover, enemy, alpha, beta);
        if (result == NO_MOVE) {
            final int enemyResult = moverResult3(counter, empties, enemy, mover, -beta, -alpha);
            if (enemyResult == NO_MOVE) {
                return BitBoardUtils.terminalScore(mover, enemy);
            } else {
                return -enemyResult;
            }
        } else {
            return result;
        }
    }

    /**
     * alpha, beta, result from mover's point of view.
     *
     * @return solve value according to fail-soft alpha/beta, unless there are no legal moves in which case it returns NO_MOVE.
     */
    private static int moverResult3(Counter counter, ListOfEmpties empties, long mover, long enemy, int alpha, int beta) {
        int result = NO_MOVE;
        for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
            final Square square = node.square;
            final long flips = counter.calcFlips(square, mover, enemy);
            if (flips != 0) {
                final long subMover = enemy & ~flips;
                final long subEnemy = mover | flips | square.placement();
                node.remove();
                final int subResult = -solve2(counter, subMover, subEnemy, -beta, -alpha, empties.first().square, empties.first().next.square);
                node.restore();
                if (subResult > result) {
                    result = subResult;
                    if (subResult > alpha) {
                        if (subResult >= beta) {
                            return result;
                        }
                        alpha = subResult;
                    }
                }
            }
        }
        return result;
    }

    static int solve2(Counter counter, long mover, long enemy, int alpha, int beta, Square empty1, Square empty2) {
        final int result = moverResult2(counter, mover, enemy, beta, empty1, empty2);
        if (result == NO_MOVE) {
            final int enemyResult = moverResult2(counter, enemy, mover, -alpha, empty1, empty2);
            if (enemyResult == NO_MOVE) {
                return BitBoardUtils.terminalScore(mover, enemy);
            } else {
                return -enemyResult;
            }
        } else {
            return result;
        }
    }

    /**
     * alpha, beta, result from mover's point of view.
     *
     * @return solve value according to fail-soft alpha/beta, unless there are no legal moves in which case it returns NO_MOVE.
     */
    private static int moverResult2(Counter counter, long mover, long enemy, int beta, Square empty1, Square empty2) {
        int result = NO_MOVE;
        final long flips1 = counter.calcFlips(empty1, mover, enemy);
        if (flips1 != 0) {
            final long subMover = enemy & ~flips1;
            final long subEnemy = mover | flips1 | empty1.placement();
            final int subResult = -solve1(counter, subMover, subEnemy, empty2);
            if (subResult > result) {
                result = subResult;
                if (subResult >= beta) {
                    return result;
                }
            }
        }

        final long flips2 = counter.calcFlips(empty2, mover, enemy);
        if (flips2 != 0) {
            final long subMover = enemy & ~flips2;
            final long subEnemy = mover | flips2 | empty2.placement();
            final int subResult = -solve1(counter, subMover, subEnemy, empty1);
            if (subResult > result) {
                result = subResult;
            }
        }

        return result;
    }

    /**
     * Solve when there is only 1 empty square.
     *
     * @param mover mover bitboard
     * @param enemy enemy bitboard
     * @param empty the empty square
     * @return solve value, exact.
     */
    static int solve1(Counter counter, long mover, long enemy, Square empty) {
        final long moverFlips = counter.calcFlips(empty, mover, enemy);
        if (moverFlips != 0) {
            mover |= moverFlips;
            final int net = 2 * bitCount(mover) - 62; // -62 because we didn't set the placed disk
            return net;
        }
        final long enemyFlips = counter.calcFlips(empty, enemy, mover);
        if (enemyFlips != 0) {
            enemy |= enemyFlips;
            final int net = 62 - 2 * bitCount(enemy); // 62 because we didn't set the placed disk
            return net;
        }
        int net = 2 * bitCount(mover) - 63; // 63 because 1 empty square remains
        if (BitBoardUtils.WINNER_GETS_EMPTIES) {
            if (net > 0) {
                net++;
            } else {
                // net can't be 0 because 1 empty remains, so must be negative
                net--;
            }
        }
        return net;
    }

    /**
     * Solve a position sorting moves only by parity and fixed-square location, not by examining the subsequent position.
     *
     * @param movesToCheck bitBoard containing moves to check (if the mover moves).
     */
    private static int solveNoSort(Counter counter, long mover, long enemy, int alpha, int beta, ListOfEmpties empties, int nEmpties, long parity,
                                   long movesToCheck) {
        if (nEmpties < MIN_PARITY_DEPTH) {
            return solveNoParity(counter, mover, enemy, alpha, beta, empties, nEmpties);
        }
        final int result = moverResultNoSort(counter, mover, enemy, alpha, beta, empties, nEmpties, parity, movesToCheck);
        if (result == NO_MOVE) {
            final int enemyResult = moverResultNoSort(counter, enemy, mover, -beta, -alpha, empties, nEmpties, parity, -1L);
            if (enemyResult == NO_MOVE) {
                return BitBoardUtils.terminalScore(mover, enemy);
            } else {
                return -enemyResult;
            }
        } else {
            return result;
        }
    }

    static int moverResultNoSort(Counter counter, long mover, long enemy, int alpha, int beta, ListOfEmpties empties, int nEmpties, long parity, long movesToCheck) {
        int result = NO_MOVE;

        // sort by parity only
        for (int desiredParity = 1; desiredParity >= 0; desiredParity--) {
            for (ListOfEmpties.Node node = empties.first(); node != empties.end; node = node.next) {
                // parity nodes first
                final Square square = node.square;
                if (BitBoardUtils.isBitClear(movesToCheck, square.sq)) {
                    continue;
                }
                if (BitBoardUtils.getBit(parity, square.sq) == desiredParity) {
                    final long flips = counter.calcFlips(square, mover, enemy);
                    if (flips != 0) {
                        final long subMover = enemy & ~flips;
                        final long subEnemy = mover | flips | square.placement();
                        node.remove();
                        final int subResult = -solveNoSort(counter, subMover, subEnemy, -beta, -alpha, empties, nEmpties - 1
                                , parity ^ square.parityRegion, -1L);
                        node.restore();
                        if (subResult > result) {
                            result = subResult;
                            if (subResult > alpha) {
                                if (subResult >= beta) {
                                    return result;
                                }
                                alpha = subResult;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
