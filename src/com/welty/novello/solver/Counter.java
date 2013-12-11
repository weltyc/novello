package com.welty.novello.solver;

import com.welty.novello.core.Counts;
import com.welty.novello.core.Square;
import com.welty.novello.eval.CoefficientEval;
import com.welty.novello.eval.Eval;
import com.welty.novello.eval.Mpc;
import org.jetbrains.annotations.NotNull;

/**
 * Like an Eval, but keeps track of node counts.
 * <p/>
 * Unlike an Eval, this is not thread-safe. So create one for each thread.
 *
 * This does not implement the Eval interface for two reasons:
 * 1. Evals are assumed to be thread-safe, and this isn't.
 * 2. To avoid vtbl lookups for speed.
 */
public class Counter {
    private final @NotNull Eval eval;
    private long nEvals;
    private long nFlips;
    final @NotNull Mpc mpcs;

    public Counter(@NotNull Eval eval) {
        this.eval = eval;
        if (eval instanceof CoefficientEval) {
            mpcs = ((CoefficientEval)eval).mpc;
        } else {
            mpcs = Mpc.NULL;
        }
    }

    public long nFlips() {
        return nFlips;
    }

    public long calcFlips(Square square, long mover, long enemy) {
        nFlips++;
        return square.calcFlips(mover, enemy);
    }

    public int eval(long mover, long enemy) {
        nEvals++;
        return eval.eval(mover, enemy);
    }

    public long nEvals() {
        return nEvals;
    }

    public @NotNull Counts getNodeStats() {
        return new Counts(nFlips, nEvals);
    }
}
