package com.welty.othello.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

/**
 * A menu that allows the user to choose a selected level
 */
class LevelMenu extends JMenu {
    private static final String KEY = "Level";

    /**
     * Get the selected level.
     *
     * @return the selected level.
     */
    int getSelectedLevel() {
        return selectedLevel;
    }

    private volatile int selectedLevel;

    public LevelMenu(int... levels) {
        super("Level");
        setMnemonic('L');

        selectedLevel = Preferences.userNodeForPackage(LevelMenuItem.class).getInt(KEY, 2);
        final ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();

        for (int level : levels) {
            final LevelMenuItem menuItem = new LevelMenuItem(level, selectedLevel);
            add(menuItem);
            buttonGroup.add(menuItem);
        }
    }

    private class LevelMenuItem extends JRadioButtonMenuItem {
        private LevelMenuItem(int level, int selected) {
            super(new MyAction(level));
            setMnemonic(Integer.toString(level % 10).charAt(0));
            setSelected(level ==selected);
        }
    }

    private class MyAction extends AbstractAction {
        private final int level;

        public MyAction(int level) {
            super(Integer.toString(level));
            this.level = level;
        }

        @Override public void actionPerformed(ActionEvent e) {
            Preferences.userNodeForPackage(LevelMenuItem.class).putInt(KEY, level);
            selectedLevel = level;
        }
    }
}
