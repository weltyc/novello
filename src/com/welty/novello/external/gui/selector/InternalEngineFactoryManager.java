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

package com.welty.novello.external.gui.selector;

import com.orbanova.common.feed.Mapper;
import com.welty.novello.selfplay.Players;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.welty.novello.external.gui.selector.InternalEngineFactory.of;

public class InternalEngineFactoryManager {
    final Collection<InternalEngineFactory> weakFactories;

    // cutoffs:
    //  BEGINNER <
    // Charlie 2 <= EASY <
    // Gertrude 1 <= MEDIUM <
    // Keiko 1 <= ADVANCED <
    // Novello 2 <= HARD
    private static final String BEGINNER = "<html>Strength: <span bgcolor='#AAAAFF'>Beginner</span></html>";
    private static final String EASY = "<html>Strength: <span bgcolor='#44FF44'>Easy</span></html>";
    private static final String MEDIUM = "<html>Strength: <span bgcolor='#FFFF00'>Medium</span></html>";
    private static final String ADVANCED = "<html>Strength: <span bgcolor='#FFC800'>Advanced</span></html>";
    private static final String HARD = "<html>Strength: <span bgcolor='#FF8888'>Hard</span></html>";
    private static final InternalEngineFactory novelloFactory = InternalEngineFactory.of("Novello", true, "", Players.eval("ntestJ"), new Mapper<Integer, String>() {
        @NotNull @Override public String y(Integer x) {
            return x <= 1 ? ADVANCED : HARD;
        }
    }
    );

    public static final InternalEngineFactory ABIGAIL = of("Abigail", false, "NS", new SimpleEval() {
        @Override public int eval(SimpleEval.Situation s) {
            return s.netDisks();
        }
    },
            BEGINNER
    );

    public static final InternalEngineFactoryManager instance = new InternalEngineFactoryManager();

    private InternalEngineFactoryManager() {
        final List<InternalEngineFactory> mutableSelectors = Arrays.asList(
                ABIGAIL,
                of("Charlie", false, "NS", new SimpleEval() {
                            @Override public int eval(Situation s) {
                                return s.netDisks() + 9 * s.netCorners();
                            }
                        },

                        new Mapper<Integer, String>() {
                            @NotNull @Override public String y(Integer x) {
                                return x <= 1 ? BEGINNER : EASY;
                            }
                        }
                ),
                of("Ethelred", false, "NS", new SimpleEval() {
                    @Override public int eval(Situation s) {
                        return s.netDisks() + s.corner2Value();
                    }
                }, EASY),
                of("Gertrude", false, "NS", new SimpleEval() {
                    @Override public int eval(Situation s) {
                        return s.interpolate(s.netMobs() + s.corner2Value());
                    }
                }, MEDIUM),
                of("Ivan", false, "NS", new SimpleEval() {
                            @Override public int eval(Situation s) {
                                return s.interpolate(s.netMobs() + s.corner2Value() + s.netPotMobs() / 2);
                            }
                        },
                        new Mapper<Integer, String>() {
                            @NotNull @Override public String y(Integer x) {
                                return x <= 1 ? MEDIUM : ADVANCED;
                            }
                        }
                ),
                of("Keiko", false, "", new SimpleEval() {
                    @Override public int eval(Situation s) {
                        return s.interpolate(s.netMobs() + s.corner2Value() + s.netPotMobs() / 2);
                    }
                }, ADVANCED)

        );
        weakFactories = Collections.unmodifiableCollection(mutableSelectors);
    }

    /**
     * Create a list of internal opponent factories
     *
     * @return the list
     */
    public static List<EngineFactory> internalOpponentSelectors(boolean includeWeakEngines) {
        final ArrayList<EngineFactory> selectors = new ArrayList<>();

        if (includeWeakEngines) {
            selectors.addAll(instance.weakFactories);
        }
        selectors.add(novelloFactory);

        return selectors;
    }

    /**
     * Get an EngineFactory by name
     *
     * @param name engine name
     * @return the EngineFactory with that name, or null if there is no such engine selector.
     */
    public EngineFactory get(@NotNull String name) {
        for (EngineFactory selector : internalOpponentSelectors(true)) {
            if (selector.name.equals(name)) {
                return selector;
            }
        }
        return null;
    }
}
