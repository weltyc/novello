package com.welty.othello.gui;

import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Position;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * View of the Othello Board
 */
public class BoardPanel extends JPanel implements GameView.ChangeListener {
    private static final Color BOARD_COLOR = new Color(0x00, 0x60, 0x00);

    private final @NotNull GameView gameView;

    BoardPanel(@NotNull GameView gameView) {
        this.gameView = gameView;
        gameView.addChangeListener(this);
        // find out ASAP whether images are available.
        Images.loadImages();

        final Dimension dimension = new Dimension(400, 400);
        setPreferredSize(dimension);
        setBackground(BOARD_COLOR);
        addMouseListener(new MyMouseListener());
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Position position = gameView.getPosition();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                final int sq = BitBoardUtils.square(row, col);
                final BufferedImage image;
                if (BitBoardUtils.isBitSet(position.black, sq)) {
                    image = Images.black;
                } else if (BitBoardUtils.isBitSet(position.white, sq)) {
                    image = Images.white;
                } else {
                    image = Images.empty;
                }
                final int x = 350-col*50;
                final int y = 350-row*50;
                g.drawImage(image, x, y, null);
            }
        }
    }

    @Override public void gameViewChanged() {
        repaint();
    }

    private class MyMouseListener extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            final Point point = e.getPoint();
            final int col = 7 - point.x/50;
            final int row = 7- point.y/50;

            if (!BitBoardUtils.badRowNum(row) && !BitBoardUtils.badRowNum(col)) {
                final int sq = BitBoardUtils.square(row, col);
                gameView.boardClick(sq);
            }
        }
    }
}
