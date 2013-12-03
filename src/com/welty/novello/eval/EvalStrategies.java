package com.welty.novello.eval;

import java.util.*;

/**
 * Utility class containing EvalStrategy instances
 */
@SuppressWarnings("OctalInteger")
public class EvalStrategies {
    private static StrategyStore store = new StrategyStore();

    public static EvalStrategy strategy(String name) {
        return store.getStrategy(name);
    }

    public static void addStrategy(String name, EvalStrategy evalStrategy) {
        store.putStrategy(name, evalStrategy);
    }


    /**
     * Returns all known strategies at the time the call is made.
     *
     * If strategies are added after this function returns, the Iterable will not be updated to reflect that change.
     *
     * @return all strategies that have been registered.
     */
    public static Iterable<EvalStrategy> knownStrategies() {
        return store.values();
    }

    @SuppressWarnings("OctalInteger")
    public static final EvalStrategy eval1 = new EvalStrategy("eval1",
            new CornerTerm(000),
            new CornerTerm(007),
            new CornerTerm(070),
            new CornerTerm(077)
    );

    public static final EvalStrategy diagonal = new EvalStrategy("diagonal",
            new UldrTerm(0),
            new UrdlTerm(0)
    );


    static {
        new EvalStrategy("a",
                new CornerTerm2(000),
                new CornerTerm2(007),
                new CornerTerm2(070),
                new CornerTerm2(077),
                Terms.moverDisks,
                Terms.enemyDisks,
                Terms.moverMobilities,
                Terms.enemyMobilities,
                Terms.moverPotMobs,
                Terms.enemyPotMobs,
                Terms.moverPotMobs2,
                Terms.enemyPotMobs2,
                new RowTerm(0),
                new RowTerm(1),
                new RowTerm(2),
                new RowTerm(3),
                new RowTerm(4),
                new RowTerm(5),
                new RowTerm(6),
                new RowTerm(7),
                new ColTerm(0),
                new ColTerm(1),
                new ColTerm(2),
                new ColTerm(3),
                new ColTerm(4),
                new ColTerm(5),
                new ColTerm(6),
                new ColTerm(7),
                new UldrTerm(-4),
                new UldrTerm(-3),
                new UldrTerm(-2),
                new UldrTerm(-1),
                new UldrTerm(-0),
                new UldrTerm(1),
                new UldrTerm(2),
                new UldrTerm(3),
                new UldrTerm(4),
                new UrdlTerm(-4),
                new UrdlTerm(-3),
                new UrdlTerm(-2),
                new UrdlTerm(-1),
                new UrdlTerm(-0),
                new UrdlTerm(1),
                new UrdlTerm(2),
                new UrdlTerm(3),
                new UrdlTerm(4)
        );
        new EvalStrategy("b",
                new CornerTerm2(000),
                new CornerTerm2(007),
                new CornerTerm2(070),
                new CornerTerm2(077),
                Terms.moverDisks,
                Terms.enemyDisks,
                Terms.moverMobilities,
                Terms.enemyMobilities,
                Terms.moverPotMobs,
                Terms.enemyPotMobs,
                Terms.moverPotMobs2,
                Terms.enemyPotMobs2,
                new RowTerm(0),
                new RowTerm(1),
                new RowTerm(2),
                new RowTerm(3),
                new RowTerm(4),
                new RowTerm(5),
                new RowTerm(6),
                new RowTerm(7),
                new ColTerm(0),
                new ColTerm(1),
                new ColTerm(2),
                new ColTerm(3),
                new ColTerm(4),
                new ColTerm(5),
                new ColTerm(6),
                new ColTerm(7),
                new UldrTerm(-4),
                new UldrTerm(-3),
                new UldrTerm(-2),
                new UldrTerm(-1),
                new UldrTerm(-0),
                new UldrTerm(1),
                new UldrTerm(2),
                new UldrTerm(3),
                new UldrTerm(4),
                new UrdlTerm(-4),
                new UrdlTerm(-3),
                new UrdlTerm(-2),
                new UrdlTerm(-1),
                new UrdlTerm(-0),
                new UrdlTerm(1),
                new UrdlTerm(2),
                new UrdlTerm(3),
                new UrdlTerm(4),
                new CornerBlockTerm(false, false),
                new CornerBlockTerm(false, true),
                new CornerBlockTerm(true, false),
                new CornerBlockTerm(true, true)
        );
    }

    public static class StrategyStore {
        private final HashMap<String, EvalStrategy> strategyFromName = new HashMap<>();

        private synchronized void putStrategy(String name, EvalStrategy evalStrategy) {
            strategyFromName.put(name, evalStrategy);
        }

        private synchronized EvalStrategy getStrategy(String name) {
            final EvalStrategy strategy = strategyFromName.get(name);
            if (strategy == null) {
                throw new IllegalArgumentException("unknown strategy name : " + name);
            }
            return strategy;
        }

        private synchronized Collection<EvalStrategy> values() {
            return new ArrayList<>(strategyFromName.values());
        }
    }
}
