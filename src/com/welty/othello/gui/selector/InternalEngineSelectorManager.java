package com.welty.othello.gui.selector;

import com.welty.novello.selfplay.Players;

import java.util.*;

import static com.welty.othello.gui.selector.InternalEngineSelector.of;

public class InternalEngineSelectorManager {
    final Collection<InternalEngineSelector> selectors;

    public static final InternalEngineSelectorManager instance = new InternalEngineSelectorManager();

    private InternalEngineSelectorManager() {
        final List<InternalEngineSelector> mutableSelectors = Arrays.asList(
                of("Abigail", false, "NS", new SimpleEval() {
                    @Override public int eval(Situation s) {
                        return s.netDisks();
                    }
                }),
                of("Charlie", false, "NS", new SimpleEval() {
                    @Override public int eval(Situation s) {
                        return s.netDisks() + 9 * s.netCorners();
                    }
                }),
                of("Ethelred", false, "NS", new SimpleEval() {
                    @Override public int eval(Situation s) {
                        return s.netDisks() + s.corner2Value();
                    }
                }),
                of("Ethelred", false, "NS", new SimpleEval() {
                    @Override public int eval(Situation s) {
                        return s.netDisks() + s.corner2Value();
                    }
                }),
                of("Gertrude", false, "NS", new SimpleEval() {
                    @Override public int eval(Situation s) {
                        return s.interpolate(s.netMobs() + s.corner2Value());
                    }
                }),
                of("Ivan", false, "NS", new SimpleEval() {
                    @Override public int eval(Situation s) {
                        return s.interpolate(s.netMobs() + s.corner2Value() + s.netPotMobs() / 2);
                    }
                }),
                of("Keiko", false, "", new SimpleEval() {
                    @Override public int eval(Situation s) {
                        return s.interpolate(s.netMobs() + s.corner2Value() + s.netPotMobs() / 2);
                    }
                })

        );
        selectors = Collections.unmodifiableCollection(mutableSelectors);
    }

    /**
     * Create a list of internal opponent selectors
     *
     * @return the list
     */
    public static List<EngineSelector> internalOpponentSelectors(boolean includeWeakEngines) {
        final ArrayList<EngineSelector> selectors = new ArrayList<>();

        if (includeWeakEngines) {
            selectors.addAll(instance.selectors);
        }
        selectors.add(new InternalEngineSelector("Vegtbl", true, "", Players.eval("ntestJ")));

        return selectors;
    }
}
