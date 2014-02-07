package com.welty.novello.coca;

import com.welty.novello.core.MeValue;
import com.welty.novello.core.Position;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SyncPlayer;
import junit.framework.TestCase;

import java.util.List;

public class CachingMvSourceTest extends TestCase {
    public void testTwoPvs() {
        final SyncPlayer syncPlayer = new SyncPlayer(new EvalSyncEngine(Players.currentEval(), ""), 2);
        final List<MeValue> pvs = CachingMvSource.getFirstTwoPvsSearch(syncPlayer, Position.START_POSITION);
        assertEquals(2, pvs.size());
        assertEquals(Position.START_POSITION.toMr(), pvs.get(0).toMr());
        assertEquals(Position.START_POSITION.play("F5").toMr(), pvs.get(1).toMr());
    }
}
