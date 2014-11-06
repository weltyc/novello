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
import com.welty.novello.core.MinimalReflection;
import com.orbanova.common.gui.ProgressUpdater;
import com.welty.novello.eval.EvalStrategy;
import com.welty.novello.eval.PositionElement;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RarePositionMrSource implements MrSource {
    private static final Logger log = Logger.logger(RarePositionMrSource.class);

    private final EvalStrategy strategy;
    private final List<MeValue> pvs;

    /**
     * Generate a source that will produce rare subpositions
     * <p/>
     * Subpositions are rare if their patterns, according to the strategy, occur infrequently.
     * subpositions are only generated if they're not already in pvs.
     *
     * @param strategy strategy for rare position determination
     * @param pvs      source of generated rare positions.
     */
    public RarePositionMrSource(EvalStrategy strategy, List<MeValue> pvs) {
        this.strategy = strategy;
        this.pvs = pvs;
    }

    @Override public Set<MinimalReflection> getMrs() throws IOException {
        return generateRareSubpositions(strategy, pvs);
    }

    /**
     * Generate a set containing minimal reflections of all Mes that are
     * (a) subpositions of a position in positionValues
     * (b) rare, and
     * (c) not in positionValues
     *
     * @return set of minimal reflections
     */
    public static Set<MinimalReflection> generateRareSubpositions(EvalStrategy strategy, List<MeValue> pvs) {
        log.info("Starting generateRareSubpositions()");

        final int[][] countSlices = new int[64][strategy.nCoefficientIndices()];

        final Set<MinimalReflection> original = new HashSet<>();
        for (MeValue pv : pvs) {
            original.add(new MinimalReflection(pv.mover, pv.enemy));
        }

        log.info(String.format("collected source positions. %,d distinct positions from %,d pvs", original.size(), pvs.size()));

        final ProgressUpdater progressMonitor = new ProgressUpdater("Generate rare subpositions", pvs.size());

        final HashSet<MinimalReflection> mrs = new HashSet<>();
        int nextProgressReport = 25000;

        for (int i = 0; i < pvs.size(); i++) {
            final MeValue pv = pvs.get(i);

            final MinimalReflection mr = new MinimalReflection(pv.mover, pv.enemy);
            final Collection<MinimalReflection> subs = mr.subPositions();
            for (MinimalReflection sub : subs) {
                if (!original.contains(sub) && !mrs.contains(sub)) {
                    final PositionElement element = strategy.coefficientIndices(mr.mover, mr.enemy, 0);
                    final int[] counts = countSlices[mr.nEmpty()];
                    if (element.isRare(counts, 10)) {
                        mrs.add(sub);
                        element.updateHistogram(counts);
                    }
                }
            }
            if (i >= nextProgressReport) {
                progressMonitor.setProgress(i);
                log.info((i >> 10) + "k pvs processed; " + (mrs.size() >> 10) + "k rare positions generated");
                nextProgressReport *= 2;
            }
        }
        log.info("A total of " + (mrs.size() >> 10) + "k rare positions were created.");
        progressMonitor.close();
        return mrs;
    }
}
