package com.welty.novello.selfplay;

import com.welty.novello.solver.BitBoardUtils;

import static com.welty.novello.solver.BitBoardUtils.calcMoves;
import static java.lang.Long.bitCount;

/**
 */
public class Bobby extends EvalPlayer {
     Bobby() {
         super(bobbyEval);
     }

    private static final Eval bobbyEval = new Eval() {
        public int eval(long mover, long enemy) {
            final int corners = bitCount((mover & BitBoardUtils.CORNERS)) - bitCount(enemy & BitBoardUtils.CORNERS);
            final long moverMoves = calcMoves(mover, enemy);
            final long enemyMoves = calcMoves(enemy, mover);
            final int cornerCan = bitCount(moverMoves & BitBoardUtils.CORNERS) - bitCount(enemyMoves & BitBoardUtils.CORNERS);
            return 2 * corners + cornerCan;
        }
    };
}
