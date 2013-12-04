package com.welty.novello.selfplay;

import com.welty.novello.core.Position;
import com.welty.novello.eval.Eval;

/**
 * Time the EvalPlayer for performance tuning
 */
public class EvalPlayerTimer {
    public static void main(String[] args) {
        final Player player = Players.player("b1:6");
        // first game is untimed, to warm up hotspot.
        new SelfPlayGame(Position.START_POSITION, player, player, "test", false, 0).call();
        final long n0 = Eval.nEvals();

        final long t0 = System.currentTimeMillis();
        new SelfPlayGame(Position.START_POSITION, player, player, "test", false, 0).call();
        final long dt = System.currentTimeMillis() - t0;
        final long dn = Eval.nEvals() - n0;

        System.out.format("%d ms elapsed / %,d evals. %4.2f us/eval \n"  ,dt,dn , dt*1e3/dn);
    }
}
