package com.welty.othello.gui;

import com.welty.novello.selfplay.Players;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static com.orbanova.common.jsb.JSwingBuilder.*;

/**
 */
public class Viewer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                new Viewer();
            }
        });
    }

    final GameView gameView = new GameView();
    private final LevelMenu levelMenu = new LevelMenu(1, 2, 3, 4);
    private final PlayAsMenu playAsMenu = new PlayAsMenu();


    Viewer() {
        Images.loadImages();

        final AbstractAction first = new IconAction("First", Images.first, KeyEvent.VK_UP) {
            @Override public void actionPerformed(ActionEvent e) {
                gameView.first();
            }
        };

        final AbstractAction prev = new IconAction("Prev", Images.prev, KeyEvent.VK_LEFT) {
            @Override public void actionPerformed(ActionEvent e) {
                gameView.prev();
            }
        };
        final AbstractAction next = new IconAction("Next", Images.next, KeyEvent.VK_RIGHT) {
            @Override public void actionPerformed(ActionEvent e) {
                gameView.next();
            }
        };
        final AbstractAction last = new IconAction("Last", Images.last, KeyEvent.VK_DOWN) {
            @Override public void actionPerformed(ActionEvent e) {
                gameView.last();
            }
        };
        final AbstractAction newGame = new MenuAction("New", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK) {
            @Override public void actionPerformed(ActionEvent e) {
                final Engine engine = new Engine(Players.player("c1s:" + levelMenu.getSelectedLevel()));
                Engine blackEngine = playAsMenu.blackEngine(engine);
                Engine whiteEngine = playAsMenu.whiteEngine(engine);
                gameView.newGame(blackEngine, whiteEngine);
            }
        };
        final AbstractAction paste = new MenuAction("Paste", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK) {
            @Override public void actionPerformed(ActionEvent e) {
                Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);

                try {
                    if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        String text = (String)t.getTransferData(DataFlavor.stringFlavor);
                        gameView.setGameGgf(text);
                    }
                } catch(IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Can't paste game", JOptionPane.ERROR_MESSAGE);
                } catch (UnsupportedFlavorException | IOException ex) {
                    // not much we can do... ignore.
                }
            }
        };



        frame("Othello Viewer", JFrame.EXIT_ON_CLOSE,
                menuBar(
                        menu("File", 'f',
                                menuItem('N', newGame)
                        ),
                        menu("Edit", 'e',
                                menuItem('P', paste)
                        ),
                        menu("Move", 'm',
                                menuItem('F', first),
                                menuItem('P', prev),
                                menuItem('N', next),
                                menuItem('L', last)
                        ),
                        levelMenu,
                        playAsMenu
                ),
                grid(2, -1, -1,
                        new PlayerPanel(gameView),
                        hBox(
                                myButton(first), myButton(prev), myButton(next), myButton(last)
                        ).align(Component.CENTER_ALIGNMENT, Component.CENTER_ALIGNMENT),

                        new BoardPanel(gameView),
                        MoveListTableModel.of(gameView)
                )
        );
    }

    private static JButton myButton(AbstractAction action) {
        final JButton first = new JButton(action);
        first.setBorder(BorderFactory.createEmptyBorder());
        first.setHideActionText(true);
        return first;
    }

    private static abstract class IconAction extends AbstractAction {
        public IconAction(String text, BufferedImage image, int vk) {
            super(text, new ImageIcon(image));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(vk, 0));
        }
    }

    private static abstract class MenuAction extends AbstractAction {
        public MenuAction(String text, int vk, int modifiers) {
            super(text);
            //noinspection MagicConstant
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(vk, modifiers));
        }
    }
}
