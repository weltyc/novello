package com.welty.novello.selfplay;

import com.welty.novello.solver.BitBoard;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 */
public class RandomPlayer implements Player {
    private final Random random = new Random(1337);

    @Override public MoveScore calcMove(@NotNull BitBoard board, long moverMoves, int flags) {
        int n = random.nextInt(Long.bitCount(moverMoves));
        while (n-- > 0) {
            moverMoves &= moverMoves - 1;
        }
        return new MoveScore(Long.numberOfTrailingZeros(moverMoves), 0);
    }
}
