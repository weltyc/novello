package com.welty.othello.gui;

import com.orbanova.common.jsb.Grid;
import com.welty.novello.core.Position;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 */
class PlayersPanel extends Grid<JComponent> {
    PlayersPanel(GameView gameView) {
        super(2, -1, -1);
        spacing(20);

        Images.loadImages();

        setAlignmentX(Component.CENTER_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        final PlayerPanel blackPanel = new PlayerPanel(true, gameView);
        final PlayerPanel whitePanel = new PlayerPanel(false, gameView);
        whitePanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        add(blackPanel);
        add(whitePanel);
    }
}
