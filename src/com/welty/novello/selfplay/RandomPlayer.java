package com.welty.novello.selfplay;

import com.welty.novello.core.MoveScore;
import com.welty.novello.core.Position;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 */
public class RandomPlayer implements Player {
    private final Random random = new Random(1337);

    @Override public MoveScore calcMove(@NotNull Position board, long moverMoves, int searchFlags) {
        int n = random.nextInt(Long.bitCount(moverMoves));
        while (n-- > 0) {
            moverMoves &= moverMoves - 1;
        }
        return new MoveScore(Long.numberOfTrailingZeros(moverMoves), 0);
    }
}
