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

    public static EvalStrategy strategy(String name) {
        switch (name) {
            case "4":
                return eval4;
            case "5":
                return eval5;
            case "6":
                return eval6;
            case "7":
                return eval7;
            case "8":
                return eval8;
            default:
                throw new IllegalArgumentException("unknown strategy name : " + name);
        }
    }
}
