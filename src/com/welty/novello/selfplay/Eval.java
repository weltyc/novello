package com.welty.novello.selfplay;

/**
 */
public interface Eval {
    /**
     * Evaluate a position.
     * <p/>
     * It is guaranteed that the mover has a legal move.
     *
     * @param mover mover disks
     * @param enemy enemy disks
     * @return board evaluation, from mover's point of view
     */
    int eval(long mover, long enemy, long moverMoves, long enemyMoves);
}
