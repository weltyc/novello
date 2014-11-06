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

package com.welty.novello.eval;

import com.orbanova.common.misc.Logger;
import com.welty.novello.coca.*;
import com.welty.novello.core.MeValue;
import com.welty.novello.core.MinimalReflection;
import com.welty.novello.solver.Counter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates and loads MeValues for the Coefficient Calculator
 */
public class MvGenerator {
    private static final Logger log = Logger.logger(MvGenerator.class);
    private final EvalStrategy evalStrategy;
    private static final String PLAYOUT_PLAYER_NAME = "d2:16";

    public MvGenerator(EvalStrategy evalStrategy) {
        this.evalStrategy = evalStrategy;
    }

    public List<MeValue> getMvs() throws IOException {
        final List<MeValue> logbook = new LogbookMvSource().getMvs();
        final List<MeValue> pvs = new CachingMvSource(PLAYOUT_PLAYER_NAME, BaseMrSource.instance, ".pvs").getMvs();
        final MrSource rareSource = new RarePositionMrSource(evalStrategy, pvs);
        final List<MeValue> pvsx = new CachingMvSource(PLAYOUT_PLAYER_NAME, rareSource, "x.pvs").getMvs();
        final List<MeValue> pvsN = new CachingMvSource(PLAYOUT_PLAYER_NAME, NtestPvLoader.mrSource, "n.pvs").getMvs();
        final List<MeValue> pvsCap = new CachingMvSource(PLAYOUT_PLAYER_NAME, new FileMrSource(Counter.capturePath), "-cap.pvs").getMvs();
        final List<MeValue> ggs = new RandGameMvSource().getMvs();
        log.info(String.format("%,d pvs from .pvs file, %,d from x.pvs file, %,d from n.pvs file," +
                " %,d from ggs games, %,d from -cap.pvs file, %,d from logbook"
                , pvs.size(), pvsx.size(), pvsN.size(), ggs.size(), pvsCap.size(), logbook.size()));
        pvs.addAll(pvsx);
        pvs.addAll(pvsN);
        pvs.addAll(pvsCap);
        pvs.addAll(ggs);
        pvs.addAll(logbook);

        log.info("Selecting distinct pvs");
        final Set<MinimalReflection> alreadySeen = new HashSet<>();
        final List<MeValue> distinctPvs = new ArrayList<>();

        for (MeValue pv : pvs) {
            final MinimalReflection mr = new MinimalReflection(pv.mover, pv.enemy);
            if (alreadySeen.add(mr)) {
                distinctPvs.add(pv);
            }
        }
        log.info(String.format("selected %,d distinct pvs from %,d total pvs", distinctPvs.size(), pvs.size()));
        return distinctPvs;
    }
}
