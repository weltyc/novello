package com.welty.othello.gui;

import com.orbanova.common.jsb.Grid;
import com.orbanova.common.jsb.JsbFrame;
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

    private final JsbFrame frame;
    private final JMenu modeMenu;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                new Viewer();
            }
        });
    }

    final GameView gameView = new GameView();
    private final JMenu[] pveMenuItems;
    private final JMenu[] arenaMenuItems;

    private final LevelMenu levelMenu = new LevelMenu(1, 2, 3, 4);
    private final PlayAsMenu playAsMenu = new PlayAsMenu();


    Viewer() {
        Images.loadImages();

        final Action[] moveActions = createMoveActions();

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
                        String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                        gameView.setGameGgf(text);
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


        pveMenuItems = new JMenu[]{fileMenu, editMenu, moveMenu, levelMenu, playAsMenu};
        arenaMenuItems = new JMenu[]{};
        this.modeMenu = createModeMenu();


        frame = frame("Othello Viewer", JFrame.EXIT_ON_CLOSE, menuBar(modeMenu),
                grid(2, -1, -1,
                        new PlayerPanel(gameView),
                        buttonBar(moveActions).align(Component.CENTER_ALIGNMENT, Component.CENTER_ALIGNMENT),

                        new BoardPanel(gameView),
                        MoveListTableModel.of(gameView)
                )
        );

        setMenus(pveMenuItems);
    }

    private JMenu createModeMenu() {
        final AbstractAction pve = new ModeMenuItem("PvE", 'P', pveMenuItems);
        final AbstractAction arena = new ModeMenuItem("Arena", 'A', arenaMenuItems);
        final JMenu menu = menu("Mode", 'm');
        menu.add(pve);
        menu.add(arena);
        return menu;
    }

    private Action[] createMoveActions() {
        final AbstractAction first = new IconAction("First", 'F', Images.first, KeyEvent.VK_UP) {
            @Override public void actionPerformed(ActionEvent e) {
                gameView.first();
            }
        };

        final AbstractAction prev = new IconAction("Prev", 'P', Images.prev, KeyEvent.VK_LEFT) {
            @Override public void actionPerformed(ActionEvent e) {
                gameView.prev();
            }
        };
        final AbstractAction next = new IconAction("Next", 'N', Images.next, KeyEvent.VK_RIGHT) {
            @Override public void actionPerformed(ActionEvent e) {
                gameView.next();
            }
        };
        final AbstractAction last = new IconAction("Last", 'L', Images.last, KeyEvent.VK_DOWN) {
            @Override public void actionPerformed(ActionEvent e) {
                gameView.last();
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
    }

    private void setMenus(JMenu[] menuItems) {
        final JMenuBar jMenuBar = frame.getJMenuBar();
        final int n = jMenuBar.getMenuCount();
        for (int i=n; i-->0; ) {
            jMenuBar.remove(i);
        }
        jMenuBar.add(modeMenu);
        for (JMenu menu : menuItems) {
            jMenuBar.add(menu);
        }
        jMenuBar.repaint();
    }
}
