package com.welty.othello.gui;

import com.orbanova.common.jsb.Grid;

import javax.swing.*;
import java.awt.*;

/**
 */
class PlayersPanel extends Grid<JComponent> {
    PlayersPanel(GameModel gameModel) {
        super(2, -1, -1);
        spacing(20);

        Images.loadImages();

        setAlignmentX(Component.CENTER_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        final PlayerPanel blackPanel = new PlayerPanel(true, gameModel);
        final PlayerPanel whitePanel = new PlayerPanel(false, gameModel);
        whitePanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        add(blackPanel);
        add(whitePanel);
    }
}
