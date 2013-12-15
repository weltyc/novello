package com.welty.othello.gui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 */
class Images {
    static BufferedImage black;
    static BufferedImage white;
    static BufferedImage empty;

    static BufferedImage first;
    static BufferedImage prev;
    static BufferedImage next;
    static BufferedImage last;
    static BufferedImage smallBlack;
    static BufferedImage smallWhite;

    static synchronized void loadImages() {
        try {
            if (black == null) {
                black = readImage("black.PNG");
                white = readImage("white.PNG");
                empty = readImage("empty.PNG");
                first = readImage("first.GIF");
                prev = readImage("prev.GIF");
                next = readImage("next.GIF");
                last = readImage("last.GIF");
                smallBlack = readImage("smallBlack.GIF");
                smallWhite = readImage("smallWhite.GIF");
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load images");
        }
    }

    private static BufferedImage readImage(String name) throws IOException {
        return ImageIO.read(BoardPanel.class.getResourceAsStream("images/" + name));
    }
}
