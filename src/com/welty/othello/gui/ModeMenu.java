package com.welty.othello.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 */
public class ModeMenu extends JMenu {
    private final JMenuBar jMenuBar;

    public ModeMenu(JMenuBar jMenuBar) {
        super("Mode");
        setMnemonic('m');
        this.jMenuBar = jMenuBar;
    }

    public void addMenu(String text, JMenu[] menuItems) {
          add(new ModeMenuItem(text, text.charAt(0), menuItems));
    }

    public void init() {
        final JMenuItem item = getItem(0);
        item.getAction().actionPerformed(null);
    }

    private class ModeMenuItem extends AbstractAction {
        private final JMenu[] menuItems;

        public ModeMenuItem(String text, char mnemonic, JMenu[] menuItems) {
            super(text);
            this.menuItems = menuItems;
            putValue(MNEMONIC_KEY, (int)mnemonic);
        }

        @Override public void actionPerformed(ActionEvent e) {
            setMenus(menuItems);
        }

        private void setMenus(JMenu[] menuItems) {
            final int n = jMenuBar.getMenuCount();
            for (int i=n; i-->0; ) {
                jMenuBar.remove(i);
            }
            for (JMenu menu : menuItems) {
                jMenuBar.add(menu);
            }
            jMenuBar.repaint();
        }
    }
}
