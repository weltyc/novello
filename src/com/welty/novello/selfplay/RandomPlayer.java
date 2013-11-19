package com.welty.novello.selfplay;

import com.welty.novello.solver.BitBoard;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 */
public class RandomPlayer implements Player {
    private final Random random = new Random(1337);

    @Override public int calcMove(@NotNull BitBoard board) {
        long moves = board.legalMoves();
        int n = random.nextInt(Long.bitCount(moves));
        while (n-- > 0) {
            moves &= moves - 1;
        }
        return Long.numberOfTrailingZeros(moves);
    }
}
