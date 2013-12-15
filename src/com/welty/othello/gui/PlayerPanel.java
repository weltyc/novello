package com.welty.othello.gui;

import com.orbanova.common.jsb.Grid;
import com.welty.novello.core.Position;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 */
class PlayerPanel extends Grid<JComponent> implements GameView.ChangeListener {
    private final JLabel blackPlayer;
    private final JLabel whitePlayer;
    private static final int borderSize = 4;

    private final Border moverBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED), BorderFactory.createEmptyBorder(borderSize-1,borderSize-1,borderSize-1,borderSize-1));
    private final Border enemyBorder = BorderFactory.createEmptyBorder(borderSize,borderSize,borderSize,borderSize);

    private final GameView gameView;

    PlayerPanel(GameView gameView) {
        super(2, -1, -1);
        spacing(20);

        Images.loadImages();

        setAlignmentX(Component.CENTER_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.gameView = gameView;
        blackPlayer = new JLabel(new ImageIcon(Images.smallBlack));
        whitePlayer = new JLabel(new ImageIcon(Images.smallWhite));
        whitePlayer.setAlignmentX(Component.RIGHT_ALIGNMENT);

        add(blackPlayer);
        add(whitePlayer);

        gameView.addChangeListener(this);
        gameViewChanged();

    }

    @Override public void gameViewChanged() {
        blackPlayer.setText(gameView.getBlackName());
        whitePlayer.setText(gameView.getWhiteName());

        final Position position = gameView.getPosition();
        if (position.calcMoves()==0 && position.enemyMoves()==0) {
            blackPlayer.setBorder(enemyBorder);
            whitePlayer.setBorder(enemyBorder);
        }
        else if (position.blackToMove) {
            blackPlayer.setBorder(moverBorder);
            whitePlayer.setBorder(enemyBorder);
        } else {
            blackPlayer.setBorder(enemyBorder);
            whitePlayer.setBorder(moverBorder);
        }
    }
}
