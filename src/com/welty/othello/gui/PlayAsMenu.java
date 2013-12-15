package com.welty.othello.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

/**
 */
public class PlayAsMenu extends JMenu {
    private static final String KEY = "PlayAs";

    /**
     * Get the selected playAs.
     *
     * @return the selected playAs.
     */
    int getSelectedPlayAs() {
        return selectedPlayAs;
    }

    private volatile int selectedPlayAs;

    public PlayAsMenu() {
        super("Play As");
        setMnemonic('P');

        selectedPlayAs = Preferences.userNodeForPackage(PlayAsMenu.class).getInt(KEY, 0);
        if (selectedPlayAs >= texts.length) {
            selectedPlayAs = texts.length-1;
        }
        if (selectedPlayAs < 0) {
            selectedPlayAs = 0;
        }
        final ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();

        for (int level=0; level<3; level++) {
            final LevelMenuItem menuItem = new LevelMenuItem(level, selectedPlayAs);
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
        return selectedPlayAs == 0 ? null : engine;
    }

    /**
     * Determine the engine playing white
     * @param engine engine that might play white
     * @return the engine, if white should be played by an engine, or null if white should be played by the human.
     */
    public Engine whiteEngine(Engine engine) {
        return selectedPlayAs == 1 ? null : engine;
    }

    private class LevelMenuItem extends JRadioButtonMenuItem {
        private LevelMenuItem(int level, int selected) {
            super(new MyAction(level));
            setMnemonic(Integer.toString(level % 10).charAt(0));
            setSelected(level ==selected);
        }
    }

    private static String[] texts = {"Black", "White", "Neither"};

    private class MyAction extends AbstractAction {
        private final int playAs;

        public MyAction(int playAs) {
            super(texts[playAs]);
            this.playAs = playAs;
        }

        @Override public void actionPerformed(ActionEvent e) {
            Preferences.userNodeForPackage(LevelMenuItem.class).putInt(KEY, playAs);
            selectedPlayAs = playAs;
        }
    }
}
