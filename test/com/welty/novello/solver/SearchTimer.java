package com.welty.novello.solver;

import com.welty.novello.core.Move;
import com.welty.novello.core.MutableGame;
import com.welty.novello.core.Position;
import com.welty.novello.eval.CoefficientEval;
import com.welty.novello.selfplay.Player;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SelfPlayGame;

import java.util.List;

/**
 * Time the midgame search for performance tuning
 */
public class SearchTimer {
    private static final Player player = Players.player("b1:6");

    /**
     * Time searches from all positions from 12 games with 10-40 empties (so 360 positions total).
     */
    public static void main(String[] args) {
        // first game is untimed, to warm up hotspot.
        new SelfPlayGame(Position.START_POSITION, player, player, "test", 0, 0).call();
        final long n0 = CoefficientEval.nEvals();
        long nFlips = 0;

        final Search search = new Search(new Counter(Players.eval("b1")), 0);
        final int depth = 6;

        final List<MutableGame> games = SampleGames.saioGames();

        final long t0 = System.currentTimeMillis();
        for (MutableGame game : games) {
            Position pos = game.startPosition;
            for (Move move : game.getMoves()) {
                pos = pos.playOrPass(move.sq);
                final int nEmpty = pos.nEmpty();
                if (nEmpty >= 10 && nEmpty <= 40) {
                    final long moves = pos.calcMoves();
                    if (moves != 0) {
                        search.calcMove(pos, moves, depth);
                        nFlips += search.nFlips();
                    }
                }
            }
        }

        final long dt = System.currentTimeMillis() - t0;
        final long dn = CoefficientEval.nEvals() - n0;

        System.out.format("%d ms elapsed. %,d flips / %,d evals. %4.2f us/eval \n"  ,dt, nFlips, dn , dt*1e3/dn);
    }
}
