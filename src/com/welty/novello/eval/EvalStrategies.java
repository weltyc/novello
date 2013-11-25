package com.welty.novello.eval;

/**
 * Utility class containing EvalStrategy instances
 */
@SuppressWarnings("OctalInteger")
public class EvalStrategies {
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

    public static final EvalStrategy eval4 = new EvalStrategy("4",
            new CornerTerm2(000),
            new CornerTerm2(007),
            new CornerTerm2(070),
            new CornerTerm2(077)
    );

    public static final EvalStrategy eval5 = new EvalStrategy("5",
            new CornerTerm2(000),
            new CornerTerm2(007),
            new CornerTerm2(070),
            new CornerTerm2(077),
            Terms.moverDisks,
            Terms.enemyDisks,
            Terms.moverMobilities,
            Terms.enemyMobilities
    );

    public static final EvalStrategy eval6 = new EvalStrategy("6",
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

    public static EvalStrategy strategy(String name) {
        switch (name) {
            case "4":
                return eval4;
            case "5":
                return eval5;
            case "6":
                return eval6;
            default:
                throw new IllegalArgumentException("unknown strategy name : " + name);
        }
    }
}
