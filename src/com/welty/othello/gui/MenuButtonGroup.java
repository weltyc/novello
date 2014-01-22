package com.welty.othello.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

/**
 * A group of Menu Items that work together as a Radio Button group.
 * <p/>
 * This stores the most recently selected item in preferences.
 */
class MenuButtonGroup {
    private final String key;
    private final Class menuClass;
    private final String[] texts;
    volatile int selectedIndex;

    /**
     * @param key       Text key used in preferences, e.g. "Play"
     * @param menuClass Class used to determine a preferences folder
     * @param texts     text of menu items, e.g. {"Black", "White"}
     */
    MenuButtonGroup(String key, Class menuClass, String... texts) {
        this.key = key;
        this.menuClass = menuClass;
        this.texts = texts;
        selectedIndex = Preferences.userNodeForPackage(menuClass).getInt(key, 0);
        if (selectedIndex >= texts.length) {
            selectedIndex = texts.length - 1;
        }
        if (selectedIndex < 0) {
            selectedIndex = 0;
        }
    }

    /**
     * Add all items to the menu and select the default menu item
     *
     * @param menu menu to add to
     */
    void addTo(JMenu menu) {
        final ButtonGroup buttonGroup = new ButtonGroup();

        for (int i = 0; i < texts.length; i++) {
            final JRadioButtonMenuItem item = new JRadioButtonMenuItem(new MyAction(i));
            item.setSelected(i == selectedIndex);
            menu.add(item);
            buttonGroup.add(item);
        }
    }

    public String getSelectedString() {
        return texts[selectedIndex];
    }

    private class MyAction extends AbstractAction {
        private final int play;

        public MyAction(int play) {
            super(texts[play]);
            this.play = play;
            putValue(MNEMONIC_KEY, (int) texts[play].charAt(0));
        }

        @Override public void actionPerformed(ActionEvent e) {
            Preferences.userNodeForPackage(menuClass).putInt(key, play);
            selectedIndex = play;
        }
    }
}