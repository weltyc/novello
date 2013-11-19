package com.welty.novello.solver;

/**
 *  Representation of an othello move, for use in move sorting.
 *
 *  Some information about the move (e.g. flip bitboard) is stored to
 *  save time compared to recalculating it.
 */
public class Move {
    int sq;
    int score;
    long flips;
    long enemyMoves;
    ListOfEmpties.Node node;

    @Override public String toString() {
        return "Move" + BitBoardUtils.sqToText(sq) + ", enemy mobs = " + Long.bitCount(enemyMoves);
    }
}
