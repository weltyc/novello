package com.welty.novello.eval;

/**
 * Utility class containing EvalStrategy instances
 */
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

    public static final EvalStrategy eval2 = new EvalStrategy("eval2",
            new ULDRTerm(),
            new URDLTerm(),
            new RowTerm(0),
            new RowTerm(7),
            new ColTerm(0),
            new ColTerm(7)
    );

    @SuppressWarnings("OctalInteger")
    public static final EvalStrategy eval3 = new EvalStrategy("eval3",
            new CornerTerm(000),
            new CornerTerm(007),
            new CornerTerm(070),
            new CornerTerm(077),
            new ULDRTerm(),
            new URDLTerm(),
            new RowTerm(0),
            new RowTerm(7),
            new ColTerm(0),
            new ColTerm(7)
    );


    public static final EvalStrategy eval4 = new EvalStrategy("eval4",
            new CornerTerm2(000),
            new CornerTerm2(007),
            new CornerTerm2(070),
            new CornerTerm2(077)
    );

    public static final EvalStrategy eval5 = new EvalStrategy("eval5",
            new CornerTerm2(000),
            new CornerTerm2(007),
            new CornerTerm2(070),
            new CornerTerm2(077),
            Terms.moverDisks,
            Terms.enemyDisks,
            Terms.moverMobilities,
            Terms.enemyMobilities
    );

    public static final EvalStrategy current = eval5;
}
