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
    private static final Map<String, SimpleEval> fromName = new TreeMap<>();

    /**
     * @param name simple eval name
     * @return a SimpleEval with the given name, or null if there is no SimpleEval with that name.
     */
    public static SimpleEval getEval(String name) {
        return fromName.get(name);
    }

    public static Collection<String> getEvalNames() {
        return fromName.keySet();
    }

    SimpleEval(String name) {
        this.name = name;
        fromName.put(name, this);
    }

    @Override public int eval(long mover, long enemy) {
        return random.nextInt(2 * DISK_VALUE) + eval(new Situation(mover, enemy));
    }

    @Override public String toString() {
        return name;
    }

    protected abstract int eval(Situation s);

    // Initialize inbuilt players
    static {
        new SimpleEval("Abigail") {
            @Override public int eval(Situation s) {
                return s.netDisks();
            }
        };

        new SimpleEval("Charlie") {
            @Override public int eval(Situation s) {
                return s.netDisks() + 9 * s.netCorners();
            }
        };

        new SimpleEval("Ethelred") {
            @Override public int eval(Situation s) {
                return s.netDisks() + s.corner2Value();
            }
        };

        new SimpleEval("Gertrude") {
            @Override public int eval(Situation s) {
                return s.interpolate(s.netMobs() + s.corner2Value());
            }
        };

        new SimpleEval("Ivan") {
            @Override public int eval(Situation s) {
                return s.interpolate(s.netMobs() + s.corner2Value() + s.netPotMobs() / 2);
            }
        };
    }

    private static final int[] coeffs = {0, 800, -800, 500, 1000, -1000, -600, 600};

    /**
     * board situation + evaluation components for simple players
     */
    private static class Situation {
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

        private int interpolate(int eval) {
            final int nEmpty = nEmpty();
            if (nEmpty < 8) {
                eval = ((8 - nEmpty) * netDisks() + nEmpty * eval) / 8;
            }
            return eval;
        }

        private int netDisks() {
            return netCentidisks(mover, enemy);
        }

        private int netMobs() {
            return netCentidisks(moverMoves, enemyMoves);
        }

        private int netPotMobs() {
            final long empty = empty();
            return netCentidisks(BitBoardUtils.potMobs(enemy, empty), BitBoardUtils.potMobs(mover, empty));
        }

        private long empty() {
            return ~(mover | enemy);
        }

        private int netCorners() {
            return netCentidisks(mover & BitBoardUtils.CORNERS, enemy & BitBoardUtils.CORNERS);
        }

        private int corner2Value() {
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
