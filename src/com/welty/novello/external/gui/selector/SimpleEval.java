/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.external.gui.selector;


import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.eval.CornerTerm2;
import com.welty.novello.eval.Eval;

import java.util.Random;

import static com.welty.novello.eval.CoefficientCalculator.DISK_VALUE;

public abstract class SimpleEval extends Eval {
    private static final Random random = new Random();
    private final int randomness;

    SimpleEval() {
        this(2 * DISK_VALUE);
    }

    SimpleEval(int randomness) {
        this.randomness = randomness;
    }

    @Override public int eval(long mover, long enemy) {
        return random.nextInt(randomness) + eval(new Situation(mover, enemy));
    }

    protected abstract int eval(Situation s);

    private static final int[] coeffs = {0, 800, -800, 500, 1000, -1000, -600, 600};

    /**
     * board situation + evaluation components for simple players
     */
    static class Situation {
        private final long mover;
        private final long enemy;
        private final long moverMoves;
        private final long enemyMoves;

        Situation(long mover, long enemy) {
            this.mover = mover;
            this.enemy = enemy;
            this.moverMoves = BitBoardUtils.calcMoves(mover, enemy);
            this.enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        }

        private static int netCentidisks(long a, long b) {
            return DISK_VALUE * (Long.bitCount(a) - Long.bitCount(b));
        }

        int interpolate(int eval) {
            final int nEmpty = nEmpty();
            if (nEmpty < 8) {
                eval = ((8 - nEmpty) * netDisks() + nEmpty * eval) / 8;
            }
            return eval;
        }

        int netDisks() {
            return netCentidisks(mover, enemy);
        }

        int netMobs() {
            return netCentidisks(moverMoves, enemyMoves);
        }

        int netPotMobs() {
            final long empty = empty();
            return netCentidisks(BitBoardUtils.potMobs(enemy, empty), BitBoardUtils.potMobs(mover, empty));
        }

        private long empty() {
            return ~(mover | enemy);
        }

        int netCorners() {
            return netCentidisks(mover & BitBoardUtils.CORNERS, enemy & BitBoardUtils.CORNERS);
        }

        int corner2Value() {
            int eval = 0;
            for (CornerTerm2 term : CornerTerm2.terms) {
                final int instance = term.instance(mover, enemy, moverMoves, enemyMoves);
                eval += coeffs[instance];
            }
            return eval;
        }

        public int nEmpty() {
            return Long.bitCount(empty());
        }
    }
}
