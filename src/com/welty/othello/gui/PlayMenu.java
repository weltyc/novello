package com.welty.othello.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

/**
 */
public class PlayMenu extends JMenu {
    private static final String KEY = "Play";

    private volatile int selectedPlay;

    public PlayMenu() {
        super("Play");
        setMnemonic('P');

        selectedPlay = Preferences.userNodeForPackage(PlayMenu.class).getInt(KEY, 0);
        if (selectedPlay >= texts.length) {
            selectedPlay = texts.length-1;
        }
        if (selectedPlay < 0) {
            selectedPlay = 0;
        }
        final ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();

        for (int level=0; level<texts.length; level++) {
            final PlayAsMenuItem menuItem = new PlayAsMenuItem(level, selectedPlay);
            add(menuItem);
            buttonGroup.add(menuItem);
        }
    }

    /**
     * Determine the engine playing black
     * @param engine engine that might play black
     * @return the engine, if black should be played by an engine, or null if black should be played by the human.
     */
    public Engine blackEngine(Engine engine) {
        return selectedPlay == 0 ? null : engine;
    }

    /**
     * Determine the engine playing white
     * @param engine engine that might play white
     * @return the engine, if white should be played by an engine, or null if white should be played by the human.
     */
    public Engine whiteEngine(Engine engine) {
        return selectedPlay == 1 ? null : engine;
    }

    private class PlayAsMenuItem extends JRadioButtonMenuItem {
        private PlayAsMenuItem(int level, int selected) {
            super(new MyAction(level));
            setSelected(level ==selected);
        }
    }

    private static String[] texts = {"Black", "White"};

    private class MyAction extends AbstractAction {
        private final int play;

        public MyAction(int play) {
            super(texts[play]);
            this.play = play;
            setMnemonic(texts[play].charAt(0));
        }

        @Override public void actionPerformed(ActionEvent e) {
            Preferences.userNodeForPackage(PlayAsMenuItem.class).putInt(KEY, play);
            selectedPlay = play;
        }
    }
}
