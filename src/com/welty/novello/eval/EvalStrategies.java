package com.welty.novello.eval;

import java.util.HashMap;

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

    @SuppressWarnings("OctalInteger")
    public static final EvalStrategy eval1 = new EvalStrategy("eval1",
            new CornerTerm(000),
            new CornerTerm(007),
            new CornerTerm(070),
            new CornerTerm(077)
    );

    public static final EvalStrategy diagonal = new EvalStrategy("diagonal",
            new ULDRTerm(),
            new URDLTerm()
    );

    private static final EvalStrategy eval4 = new EvalStrategy("4",
            new CornerTerm2(000),
            new CornerTerm2(007),
            new CornerTerm2(070),
            new CornerTerm2(077)
    );

    private static final EvalStrategy eval5 = new EvalStrategy("5",
            new CornerTerm2(000),
            new CornerTerm2(007),
            new CornerTerm2(070),
            new CornerTerm2(077),
            Terms.moverDisks,
            Terms.enemyDisks,
            Terms.moverMobilities,
            Terms.enemyMobilities
    );

    private static final EvalStrategy eval6 = new EvalStrategy("6",
            new CornerTerm2(000),
            new CornerTerm2(007),
            new CornerTerm2(070),
            new CornerTerm2(077),
            Terms.moverDisks,
            Terms.enemyDisks,
            Terms.moverMobilities,
            Terms.enemyMobilities,
            Terms.moverPotMobs,
            Terms.enemyPotMobs
    );

    private static final EvalStrategy eval7 = new EvalStrategy("7",
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
            new RowTerm(0),
            new RowTerm(7),
            new ColTerm(0),
            new ColTerm(7)
    );

    private static final EvalStrategy eval8 = new EvalStrategy("8",
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
            new RowTerm(7),
            new ColTerm(0),
            new ColTerm(7)
    );

    private static final EvalStrategy eval9 = new EvalStrategy("9",
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
            new ColTerm(7)
    );

    public static class StrategyStore {
        private final HashMap<String, EvalStrategy> strategyFromName = new HashMap<>();

        private synchronized void putStrategy(String name, EvalStrategy evalStrategy) {
            strategyFromName.put(name, evalStrategy);
        }

        private synchronized EvalStrategy getStrategy(String name) {
            final EvalStrategy strategy = strategyFromName.get(name);
            if (strategy==null) {
                throw new IllegalArgumentException("unknown strategy name : " + name);
            }
            return strategy;
        }
    }
}
