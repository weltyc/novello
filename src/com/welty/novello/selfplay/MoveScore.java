package com.welty.novello.selfplay;

import com.welty.novello.core.BitBoardUtils;

/**
 * A move with an evaluation
*/
public class MoveScore {
    public final int sq;
    public final int score;

    public MoveScore(int sq, int score) {
        this.sq = sq;
        this.score = score;
    }

    @Override public String toString() {
        return BitBoardUtils.sqToText(sq)+"//"+score;
    }
}
