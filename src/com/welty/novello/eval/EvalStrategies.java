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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Utility class containing EvalStrategy instances
 */
@SuppressWarnings("OctalInteger")
public class EvalStrategies {
    private static StrategyStore store = new StrategyStore();

    public static EvalStrategy strategy(String name) {
        return store.getStrategy(name);
    }

    public static void addStrategy(String name, EvalStrategy evalStrategy) {
        store.putStrategy(name, evalStrategy);
    }


    /**
     * Returns all known strategies at the time the call is made.
     * <p/>
     * If strategies are added after this function returns, the Iterable will not be updated to reflect that change.
     *
     * @return all strategies that have been registered.
     */
    public static Iterable<EvalStrategy> knownStrategies() {
        return store.values();
    }

    @SuppressWarnings("OctalInteger")
    public static final EvalStrategy eval1 = new EvalStrategy("eval1",
            new CornerTerm(000),
            new CornerTerm(007),
            new CornerTerm(070),
            new CornerTerm(077)
    );

    public static final EvalStrategy diagonal = new EvalStrategy("diagonal",
            new UldrTerm(0),
            new UrdlTerm(0)
    );


    static {
        // Create the eval strategies so they exist in the strategy store
        new EvalStrategyC();
        new EvalStrategyD();
        new EvalStrategyE();
        new EvalStrategyF();
        new EvalStrategyG();
        new EvalStrategyH();
        new EvalStrategyJ();
    }

    public static class StrategyStore {
        private final HashMap<String, EvalStrategy> strategyFromName = new HashMap<>();

        private synchronized void putStrategy(String name, EvalStrategy evalStrategy) {
            final EvalStrategy previous = strategyFromName.put(name, evalStrategy);
            if (previous != null) {
                throw new IllegalArgumentException("can only have one strategy of each name");
            }
        }

        private synchronized EvalStrategy getStrategy(String name) {
            final EvalStrategy strategy = strategyFromName.get(name);
            if (strategy == null) {
                throw new IllegalArgumentException("unknown strategy name : " + name);
            }
            return strategy;
        }

        private synchronized Collection<EvalStrategy> values() {
            return new ArrayList<>(strategyFromName.values());
        }
    }

}
