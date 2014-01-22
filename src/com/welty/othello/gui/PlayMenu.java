package com.welty.othello.gui;

import javax.swing.*;

/**
 */
public class PlayMenu extends JMenu {
     private final MenuButtonGroup color;
    private final MenuButtonGroup startPosition;

    public PlayMenu() {
        super("Play");
        setMnemonic('P');

        color = new MenuButtonGroup("Play", PlayMenu.class, "Black", "White");
        color.addTo(this);
        this.addSeparator();
        final JMenu startPositionMenu = new JMenu("Start Position");
        this.add(startPositionMenu);
        startPosition = new MenuButtonGroup("StartPosition", PlayMenu.class, "Standard", "Alternate", "XOT");
        startPosition.addTo(startPositionMenu);
    }

    /**
     * Determine the engine playing black
     * @param engine engine that might play black
     * @return the engine, if black should be played by an engine, or null if black should be played by the human.
     */
    public Engine blackEngine(Engine engine) {
        return color.selectedIndex == 0 ? null : engine;
    }

    /**
     * Determine the engine playing white
     * @param engine engine that might play white
     * @return the engine, if white should be played by an engine, or null if white should be played by the human.
     */
    public Engine whiteEngine(Engine engine) {
        return color.selectedIndex == 1 ? null : engine;
    }

    public String getStartPositionType() {
        return startPosition.getSelectedString();
    }
}
