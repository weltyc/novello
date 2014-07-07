/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.novello.coca;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.MeValue;
import com.welty.novello.core.Mr;
import com.welty.novello.core.ObjectFeed;
import com.welty.novello.selfplay.EvalSyncEngine;
import com.welty.novello.selfplay.Players;
import com.welty.novello.selfplay.SelfPlaySet;
import com.welty.novello.selfplay.SyncPlayer;
import com.welty.othello.gdk.OsClock;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class BaseMrSource implements MrSource {
    private static final Logger log = Logger.logger(BaseMrSource.class);

    public static final BaseMrSource instance = new BaseMrSource();

    @Override public Set<Mr> getMrs() throws IOException {
        final int maxDepth = 8;
        final EvalSyncEngine playoutEngine = new EvalSyncEngine(Players.currentEval(), "", Players.currentEval().toString());
        final SyncPlayer playoutPlayer = new SyncPlayer(playoutEngine, maxDepth);
        final Path mrsPath = CachingMvSource.getCacheDir().resolve("base.mrs");
        if (!Files.exists(mrsPath)) {
            final Set<Mr> mrSet = new HashSet<>();
            Files.createDirectories(mrsPath.getParent());
            final SelfPlaySet.PvCollector pvCollector = new SelfPlaySet.PvCollector();
            SelfPlaySet.run(playoutPlayer, playoutPlayer, OsClock.LONG, pvCollector);
            for (MeValue pv : pvCollector.pvs) {
                mrSet.add(new Mr(pv.mover, pv.enemy));
            }
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(mrsPath)))) {
                for (Mr mr : mrSet) {
                    mr.write(out);
                }
            }
            log.info(String.format("created %s with %,d mrs", mrsPath, mrSet.size()));
        }


        return new ObjectFeed<>(mrsPath, Mr.deserializer).asSet();
    }
}
