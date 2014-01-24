package com.welty.othello.gui;

import com.orbanova.common.jsb.Grid;
import com.welty.novello.core.Position;

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

    final GameModel gameModel = new GameModel();

    private final PlayMenu playMenu = new PlayMenu();

    private final AsyncEngine engine = null;//new Engine(Players.player("ntestJ:" + levelMenu.getSelectedLevel()));

    /**
     * Viewer constructor. Must run on the Event Dispatch Thread.
     */
    Viewer() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("This must be called from the Event Dispatch Thread");
        }
        Images.loadImages();

        final Action[] moveActions = createMoveActions();

        final AbstractAction newGame = new MenuAction("New", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK) {
            @Override public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        };
        final AbstractAction paste = new MenuAction("Paste", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK) {
            @Override public void actionPerformed(ActionEvent e) {
                Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);

                try {
                    if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                        gameModel.setGameGgf(text);
                    }
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Can't paste game", JOptionPane.ERROR_MESSAGE);
                } catch (UnsupportedFlavorException | IOException ex) {
                    // not much we can do... ignore.
                }
            }
        };


        final JMenu fileMenu = menu("File", 'f',
                menuItem('N', newGame)
        );
        final JMenu editMenu = menu("Edit", 'e',
                menuItem('P', paste)
        );
        final JMenu moveMenu = createMenu("Move", 'm', moveActions);

        final JMenuBar menuBar = menuBar(fileMenu, editMenu, moveMenu, playMenu);

        frame("Othello Viewer", JFrame.EXIT_ON_CLOSE, menuBar,
                grid(2, -1, -1,
                        new PlayersPanel(gameModel),
                        buttonBar(moveActions).align(Component.CENTER_ALIGNMENT, Component.CENTER_ALIGNMENT),

                        new BoardPanel(gameModel),
                        MoveListTableModel.of(gameModel)
                )
        );


        startNewGame();
    }

    private void startNewGame() {
//        engine.setMaxDepth(levelMenu.getSelectedLevel());
        AsyncEngine blackEngine = playMenu.blackEngine(engine);
        AsyncEngine whiteEngine = playMenu.whiteEngine(engine);
        final String startPositionType = playMenu.getStartPositionType();
        final Position startPosition = StartPositionChooser.next(startPositionType);
        gameModel.newGame(blackEngine, whiteEngine, startPosition);
    }

    private Action[] createMoveActions() {
        final AbstractAction first = new IconAction("First", 'F', Images.first, KeyEvent.VK_UP) {
            @Override public void actionPerformed(ActionEvent e) {
                gameModel.first();
            }
        };

        final AbstractAction prev = new IconAction("Prev", 'P', Images.prev, KeyEvent.VK_LEFT) {
            @Override public void actionPerformed(ActionEvent e) {
                gameModel.prev();
            }
        };
        final AbstractAction next = new IconAction("Next", 'N', Images.next, KeyEvent.VK_RIGHT) {
            @Override public void actionPerformed(ActionEvent e) {
                gameModel.next();
            }
        };
        final AbstractAction last = new IconAction("Last", 'L', Images.last, KeyEvent.VK_DOWN) {
            @Override public void actionPerformed(ActionEvent e) {
                gameModel.last();
            }
        };
        return new Action[]{first, prev, next, last};
    }

    private static Grid<Component> buttonBar(Action... actions) {
        final Grid<Component> bar = hBox();
        for (Action action : actions) {
            bar.add(myButton(action));
        }
        return bar;
    }

    private static JMenu createMenu(String name, char mnemonic, Action... actions) {
        final JMenu menu = menu(name, mnemonic);
        for (Action action : actions) {
            menu.add(action);
        }
        return menu;
    }

    private static JButton myButton(Action action) {
        final JButton first = new JButton(action);
        first.setBorder(BorderFactory.createEmptyBorder());
        first.setHideActionText(true);
        return first;
    }

    private static abstract class IconAction extends AbstractAction {
        public IconAction(String text, int mnemonic, BufferedImage image, int vk) {
            super(text, new ImageIcon(image));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(vk, 0));
            putValue(MNEMONIC_KEY, mnemonic);
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
