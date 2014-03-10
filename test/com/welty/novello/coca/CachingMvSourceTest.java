package com.welty.novello.coca;

import com.welty.novello.core.Board;
import com.welty.novello.core.MeValue;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SyncPlayer;
import junit.framework.TestCase;

import java.util.List;

public class CachingMvSourceTest extends TestCase {
    public void testTwoPvs() {
        final SyncPlayer syncPlayer = new SyncPlayer(new EvalSyncEngine(Players.currentEval(), "", "current-eval"), 2);
        final List<MeValue> pvs = CachingMvSource.getFirstTwoPvsSearch(syncPlayer, Board.START_BOARD);
        assertEquals(2, pvs.size());
        assertEquals(Board.START_BOARD.toMr(), pvs.get(0).toMr());
        assertEquals(Board.START_BOARD.play("F5").toMr(), pvs.get(1).toMr());
    }
}
