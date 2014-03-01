package com.welty.othello.gui.selector;

import com.orbanova.common.feed.Mapper;
import com.welty.novello.selfplay.Players;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.welty.othello.gui.selector.InternalEngineSelector.of;

public class InternalEngineSelectorManager {
    final Collection<InternalEngineSelector> weakSelectors;

    // cutoffs:
    //  BEGINNER <
    // Charlie 2 <= EASY <
    // Gertrude 1 <= MEDIUM <
    // Keiko 1 <= ADVANCED <
    // Vegtbl 2 <= HARD
    private static final String BEGINNER = "<html>Strength: <span bgcolor='#AAAAFF'>Beginner</span></html>";
    private static final String EASY = "<html>Strength: <span bgcolor='#44FF44'>Easy</span></html>";
    private static final String MEDIUM = "<html>Strength: <span bgcolor='#FFFF00'>Medium</span></html>";
    private static final String ADVANCED = "<html>Strength: <span bgcolor='#FFC800'>Advanced</span></html>";
    private static final String HARD = "<html>Strength: <span bgcolor='#FF8888'>Hard</span></html>";
    private static final InternalEngineSelector vegtblSelector = InternalEngineSelector.of("Vegtbl", true, "", Players.eval("ntestJ"), new Mapper<Integer, String>() {
        @NotNull @Override public String y(Integer x) {
            return x <= 1 ? ADVANCED : HARD;
        }
    }
    );

    public static final InternalEngineSelector ABIGAIL = of("Abigail", false, "NS", new SimpleEval() {
        @Override public int eval(SimpleEval.Situation s) {
            return s.netDisks();
        }
    },
            BEGINNER
    );

    public static final InternalEngineSelectorManager instance = new InternalEngineSelectorManager();

    private InternalEngineSelectorManager() {
        final List<InternalEngineSelector> mutableSelectors = Arrays.asList(
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
        weakSelectors = Collections.unmodifiableCollection(mutableSelectors);
    }

    /**
     * Create a list of internal opponent weakSelectors
     *
     * @return the list
     */
    public static List<EngineSelector> internalOpponentSelectors(boolean includeWeakEngines) {
        final ArrayList<EngineSelector> selectors = new ArrayList<>();

        if (includeWeakEngines) {
            selectors.addAll(instance.weakSelectors);
        }
        selectors.add(vegtblSelector);

        return selectors;
    }

    /**
     * Get an EngineSelector by name
     *
     * @param name engine name
     * @return the EngineSelector with that name, or null if there is no such engine selector.
     */
    public EngineSelector get(@NotNull String name) {
        for (EngineSelector selector : internalOpponentSelectors(true)) {
            if (selector.name.equals(name)) {
                return selector;
            }
        }
        return null;
    }
}
