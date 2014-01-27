package com.welty.novello.eval;


import com.welty.novello.core.BitBoardUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static com.welty.novello.eval.CoefficientCalculator.DISK_VALUE;

public abstract class SimpleEval extends Eval {
    private static final Random random = new Random();
    private final String name;
    private static final Map<String, SimpleEval> evalFromName = new TreeMap<>();

    /**
     * @param name simple eval name
     * @return a SimpleEval with the given name, or null if there is no SimpleEval with that name.
     */
    public static SimpleEval getEval(String name) {
        return evalFromName.get(name);
    }

    SimpleEval(String name) {
        this.name = name;
        evalFromName.put(name, this);
    }

    @Override public int eval(long mover, long enemy) {
        final long moverMoves = BitBoardUtils.calcMoves(mover, enemy);
        final long enemyMoves = BitBoardUtils.calcMoves(enemy, mover);
        return random() + eval(mover, enemy, moverMoves, enemyMoves);
    }

    protected abstract int eval(long mover, long enemy, long moverMoves, long enemyMoves);

    private static final int[] coeffs = {0, 800, -800, 500, 1000, -1000, -600, 600};

    private static int corner2Value(long mover, long enemy, long moverMoves, long enemyMoves) {
        int eval = 0;
        for (CornerTerm2 term : CornerTerm2.terms) {
            final int instance = term.instance(mover, enemy, moverMoves, enemyMoves);
            eval += coeffs[instance];
        }
        return eval;
    }

    private static int random() {
        return random.nextInt(2 * DISK_VALUE);
    }

    private static int netDisks(long mover, long enemy) {
        return Long.bitCount(mover) - Long.bitCount(enemy);
    }

    public static Eval Greedy = new SimpleEval("Abigail") {
        @Override public int eval(long mover, long enemy, long moverMoves, long enemyMoves) {
            return DISK_VALUE * netDisks(mover, enemy);
        }
    };

    public static Eval Corny = new SimpleEval("Charlie") {
        @Override public int eval(long mover, long enemy, long moverMoves, long enemyMoves) {
            final int netCorners = netDisks(mover & BitBoardUtils.CORNERS, enemy & BitBoardUtils.CORNERS);
            return DISK_VALUE * (netDisks(mover, enemy) + 9 * netCorners);
        }
    };

    public static Eval Nox = new SimpleEval("Ethelred") {

        @Override public int eval(long mover, long enemy, long moverMoves, long enemyMoves) {
            int eval = corner2Value(mover, enemy, moverMoves, enemyMoves);
            return eval + netDisks(mover, enemy) * DISK_VALUE;
        }
    };

    public static Eval Mob = new SimpleEval("Gertrude") {
        @Override public int eval(long mover, long enemy, long moverMoves, long enemyMoves) {
            int eval = DISK_VALUE*(netDisks(moverMoves, enemyMoves)) + corner2Value(mover, enemy, moverMoves, enemyMoves);
            final int nEmpty = BitBoardUtils.nEmpty(mover, enemy);
            if (nEmpty < 8) {
                eval = ((8-nEmpty)*netDisks(mover, enemy) + nEmpty*eval)/ 8;
            }
            return eval;
        }
    };

    @Override public String toString() {
        return name;
    }

    public static Collection<String> getEvalNames() {
        return evalFromName.keySet();
    }
}
