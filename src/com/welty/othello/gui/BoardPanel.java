package com.welty.othello.gui;

import com.orbanova.common.misc.Logger;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Position;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * View of the Othello Board
 */
public class BoardPanel extends JPanel implements GameView.ChangeListener {
    private static final Color BOARD_COLOR = new Color(0x00, 0x60, 0x00);
    private Position position;
    private Position prevPosition;
    private static final Logger log = Logger.logger(BoardPanel.class);
    /**
     * Fade index, from 0..MAX_FADE
     */
    private int fadeIndex = 0;
    /**
     * Time to switch to next Fade index, in ms.
     */
    private static final int speed = 100;

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
        prevPosition = position = gameView.getPosition();

        ActionListener fadeTask = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (fadeIndex < FadeImages.MAX_FADE) {
                    fadeIndex++;
                    log.info("Fade task");
                    repaint();
                }
            }
        };
        Timer timer = new Timer(speed, fadeTask);
        timer.start();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        paint(g, fadeIndex, prevPosition, position);
    }

    private void paint(Graphics g, int iFade, Position from, Position to) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                final int sq = BitBoardUtils.square(row, col);
                final BufferedImage image = FadeImages.getFadeImage(iFade, from, to, sq);
                final int x = 350 - col * 50;
                final int y = 350 - row * 50;
                g.drawImage(image, x, y, null);
            }
        }
    }

    @Override public void gameViewChanged() {
        prevPosition = position;
        position = gameView.getPosition();
        fadeIndex = 0;
        repaint();
    }

    private class MyMouseListener extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            final Point point = e.getPoint();
            final int col = 7 - point.x / 50;
            final int row = 7 - point.y / 50;

            if (!BitBoardUtils.badRowNum(row) && !BitBoardUtils.badRowNum(col)) {
                final int sq = BitBoardUtils.square(row, col);
                gameView.boardClick(sq);
            }
        }
    }

}

class FadeImages {
    enum Piece {
        BLACK, WHITE, EMPTY
    }

    static int MAX_FADE = 4;

    private static BufferedImage[] blackWhiteFades = new BufferedImage[MAX_FADE + 1];
    private static BufferedImage[] blackEmptyFades = new BufferedImage[MAX_FADE + 1];
    private static BufferedImage[] whiteEmptyFades = new BufferedImage[MAX_FADE + 1];

    static {
        Images.loadImages();
        for (int i = 0; i <= MAX_FADE; i++) {
            blackWhiteFades[i] = createFade(i, Images.black, Images.white);
            blackEmptyFades[i] = createFade(i, Images.black, Images.empty);
            whiteEmptyFades[i] = createFade(i, Images.white, Images.empty);
        }
    }

    private static BufferedImage createFade(int fade, BufferedImage black, BufferedImage white) {
        final int w = black.getWidth();
        final int h = black.getHeight();
        final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final int rgb = fadePixel(fade, black.getRGB(x, y), white.getRGB(x, y));
                out.setRGB(x, y, rgb);
            }
        }
        return out;
    }

    private static int fadePixel(int fade, int from, int to) {
        final Color fc = new Color(from);
        final Color tc = new Color(to);
        final int red = fadeSubPixel(fade, fc.getRed(), tc.getRed());
        final int green = fadeSubPixel(fade, fc.getGreen(), tc.getGreen());
        final int blue = fadeSubPixel(fade, fc.getBlue(), tc.getBlue());
        return new Color(red, green, blue).getRGB();
    }

    private static int fadeSubPixel(int fade, int from, int to) {
        return ((fade * to) + (MAX_FADE - fade) * from) / MAX_FADE;
    }

    static BufferedImage getFadeImage(int fadeIndex, Position from, Position to, int sq) {
        final Piece fromPiece = getPiece(from, sq);
        final Piece toPiece = getPiece(to, sq);
        return getFadeImage(fadeIndex, fromPiece, toPiece);
    }

    private static Piece getPiece(Position from, int sq) {
        if (BitBoardUtils.isBitSet(from.black, sq)) {
            return Piece.BLACK;
        } else if (BitBoardUtils.isBitSet(from.white, sq)) {
            return Piece.WHITE;
        } else {
            return Piece.EMPTY;
        }
    }

    static BufferedImage getFadeImage(int fadeIndex, Piece from, Piece to) {
        if (from == to) {
            switch (from) {
                case BLACK:
                    return Images.black;
                case WHITE:
                    return Images.white;
                case EMPTY:
                    return Images.empty;
            }
        }

        if (from.ordinal() > to.ordinal()) {
            final Piece temp = from;
            from = to;
            to = temp;
            fadeIndex = MAX_FADE - fadeIndex;
        }

        if (from == Piece.BLACK) {
            if (to == Piece.WHITE) {
                return blackWhiteFades[fadeIndex];
            } else {
                return blackEmptyFades[fadeIndex];
            }
        } else {
            return whiteEmptyFades[fadeIndex];
        }
    }
}
