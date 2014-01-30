package com.welty.othello.gui;

import com.orbanova.common.feed.Feeds;
import com.orbanova.common.jsb.Grid;
import com.orbanova.common.jsb.JsbGridLayout;
import com.orbanova.common.jsb.JsbTextField;
import com.welty.novello.eval.SimpleEval;
import com.welty.othello.core.OperatingSystem;
import com.welty.othello.gui.prefs.PrefInt;
import com.welty.othello.gui.prefs.PrefString;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;

import static com.orbanova.common.jsb.JSwingBuilder.*;

/**
 */
public class OpponentSelectionWindow {

    private static final PrefInt levelPref = new PrefInt(OpponentSelectionWindow.class, "Level", 1);
    private static final PrefString enginePref = new PrefString(OpponentSelectionWindow.class, "Opponent", "Abigail");
    /**
     * Data used to initialize engineSelectors on startup.
     */
    private static final java.util.List<EngineSelector> ENGINE_SELECTORS = new ArrayList<>();

    static {
        for (String name : SimpleEval.getEvalNames()) {
            ENGINE_SELECTORS.add(new InternalEngineSelector(name));
        }
        ENGINE_SELECTORS.add(new InternalEngineSelector("Vegtbl", true, "d2", ""));
    }


    private static OpponentSelectionWindow instance;

    private final JDialog frame;
    private final JList<Integer> levels = new JList<>();
    private static final EngineListModel engineListModel = new EngineListModel(ENGINE_SELECTORS);
    private final JList<EngineSelector> engineSelectors = new JList<>(engineListModel);

    // these are written to when the user clicks "OK"
    private int selectedLevel;
    private @NotNull EngineSelector selectedEngine;

    public synchronized static OpponentSelectionWindow getInstance() {
        if (instance == null) {
            instance = new OpponentSelectionWindow();
        }
        return instance;
    }

    private OpponentSelectionWindow() {
        // Level selection list box.
        // Need to create this before Opponent selection list box because the
        // Opponent selection list box modifies it.
        final DefaultListModel<Integer> levelModel = new DefaultListModel<>();
        setLevelElements(levelModel, EngineSelector.advancedLevels);
        levels.setModel(levelModel);
        setUpList(levels);
        levels.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        levels.setVisibleRowCount(EngineSelector.advancedLevels.length / 2);


        // Opponent selection list box.
        engineSelectors.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    final EngineSelector engineSelector = engineSelectors.getSelectedValue();
                    setLevelElements(levelModel, engineSelector.availableLevels);
                    levels.setSelectedIndex(findNearestLevel(selectedLevel, engineSelector.availableLevels));
                }
            }
        });
        setUpList(engineSelectors);

        selectUsersPreferredEngine();
        selectUsersPreferredLevel();


        final JButton ok = button(new AbstractAction("OK") {
            @Override public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                selectedLevel = levels.getSelectedValue();
                levelPref.put(levels.getSelectedValue());
                selectedEngine = engineSelectors.getSelectedValue();
                enginePref.put(selectedEngine.name);
            }
        });


        final JButton cancel = button(new AbstractAction("Cancel") {
            @Override public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);

            }
        });

        final JButton addEngine = button(new AbstractAction("Add engine...") {
            @Override public void actionPerformed(ActionEvent e) {
                new AddEngineDialog(frame);
            }
        });

        frame = new JDialog(null, "Select Opponent", Dialog.ModalityType.APPLICATION_MODAL);
        frame.setLayout(new JsbGridLayout(1));
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.add(
                vBox(
                        grid(2, 0, -1,
                                wrap("Opponent", engineSelectors), wrap("Level", levels)
                        ),
                        buttonBar(false, addEngine),
                        buttonBar(true, ok, cancel)
                )
        );
        frame.pack();
        frame.setVisible(false);

        frame.getRootPane().setDefaultButton(ok);
    }

    private void selectUsersPreferredLevel() {
        selectedLevel = levelPref.get();
        levels.setSelectedIndex(findNearestLevel(selectedLevel, selectedEngine.availableLevels));
        selectedLevel = levels.getSelectedValue();
    }

    /**
     * Select the User's preferred engine both in the dialog box and in the data model.
     */
    private void selectUsersPreferredEngine() {
        final String preferredEngineName = enginePref.get();
        final int i = engineListModel.find(preferredEngineName);
        engineSelectors.setSelectedIndex(Math.max(0, i));
        selectedEngine = engineSelectors.getSelectedValue();
    }

    private static JComponent wrap(String title, JList list) {
        final Dimension preferredSize = list.getPreferredSize();
        list.setBorder(null);
        final JLabel jLabel = new JLabel(title);
        jLabel.setFont(UIManager.getFont("TitledBorder.font"));
        final int minWidth = 50 + Math.max(preferredSize.width, jLabel.getPreferredSize().width);
        System.out.println("min width = " + minWidth);
        final JScrollPane scrollPane = scrollPane(list);
        scrollPane.setPreferredSize(new Dimension(minWidth, 50 + preferredSize.height));
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), title));
        return scrollPane;
    }

    /**
     * Set the contents of the ListModel to the given levels.
     */
    private static void setLevelElements(DefaultListModel<Integer> ListModel, Integer[] levels) {
        ListModel.removeAllElements();
        for (Integer level : levels) {
            ListModel.addElement(level);
        }
    }

    /**
     * Find the index of the highest level <= targetLevel.
     * <p/>
     * This implementation assumes the levels are in order.
     *
     * @param targetLevel desired search depth
     * @param levels      available search depth
     * @return index of search depth
     */
    private static int findNearestLevel(int targetLevel, Integer[] levels) {
        int i;
        for (i = 0; i < levels.length; i++) {
            if (levels[i] > targetLevel) {
                break;
            }
        }
        if (i > 0) {
            i--;
        }

        return i;
    }

    private static <T> void setUpList(JList<T> ops) {
        ops.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        ops.setFont(UIManager.getFont("TextField.font"));
        ops.setAlignmentY(0.0f);
        ops.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ops.setSelectedIndex(0);
    }

    /**
     * Get the Engine that the user has selected
     *
     * @return the Engine
     */
    public AsyncEngine getEngine() {
        return new Opponent(selectedEngine, selectedLevel).getAsyncEngine();
    }

    /**
     * Display this window
     */
    public void show() {
        frame.setVisible(true);
    }

    static class Opponent {
        private final EngineSelector engineSelector;
        private final int level;

        Opponent(@NotNull EngineSelector engineSelector, int level) {
            this.engineSelector = engineSelector;
            this.level = level;
        }

        public AsyncEngine getAsyncEngine() {
            return AsyncEngineManager.getOrCreate(engineSelector, level);
        }
    }

    private static class AddEngineDialog extends JDialog {
        AddEngineDialog(Window parent) {
            super(parent, "Add Engine", ModalityType.APPLICATION_MODAL);
            final JsbTextField nameField = textField();
            final JsbTextField wdField = textField();
            wdField.setPreferredSize(new Dimension(300, wdField.getPreferredSize().height));
            final JsbTextField commandField = textField();
            final Grid<Component> controls = controlGrid(
                    control("Name", nameField),
                    control("Working Directory", wdField),
                    control("Command", commandField)
            );
            final JButton ok = button(new AbstractAction("OK") {
                @Override public void actionPerformed(ActionEvent e) {
                    final String name = nameField.getText();
                    if (!name.matches("[a-zA-Z0-9]+")) {
                        JOptionPane.showMessageDialog(AddEngineDialog.this, "Engine name must be alphanumeric (all characters must be a-z, A-Z, or 0-9)");
                        return;
                    }
                    final String wd = wdField.getText();
                    if (wd.contains(";")) {
                        JOptionPane.showMessageDialog(AddEngineDialog.this, "Working directory cannot contain a semicolon (;)");
                        return;
                    }
                    if (wd.isEmpty()) {
                        JOptionPane.showMessageDialog(AddEngineDialog.this, "Working directory must not be empty");
                        return;
                    }
                    final String command = commandField.getText().trim();
                    if (command.isEmpty()) {
                        JOptionPane.showMessageDialog(AddEngineDialog.this, "Command must not be empty");
                        return;
                    }
                    ExternalEngineManager.add(name, wd, command);
                    engineListModel.put(new ExternalEngineSelector(name, wd, command));
                    AddEngineDialog.this.setVisible(false);
                    AddEngineDialog.this.dispose();
                }
            });

            final JButton cancel = button(new AbstractAction("Cancel") {
                @Override public void actionPerformed(ActionEvent e) {
                    AddEngineDialog.this.setVisible(false);
                    AddEngineDialog.this.dispose();
                }
            });

            final String osName = (OperatingSystem.os == OperatingSystem.MACINTOSH) ? "Mac" : "Win";
            final String helpFile = "OpponentSelectionWindow_" + osName + ".html";
            final InputStream in = OpponentSelectionWindow.class.getResourceAsStream(helpFile);
            final String helpHtml = Feeds.ofLines(in).join("\n");

            add(vBox(
                    controls,
                    label(helpHtml),
                    buttonBar(true, ok, cancel)
            ));

            getRootPane().setDefaultButton(ok);
            pack();
            setVisible(true);
        }
    }

    private static class EngineListModel extends DefaultListModel<EngineSelector> {
        public EngineListModel(java.util.List<EngineSelector> engineSelectors) {
            for (EngineSelector es : engineSelectors) {
                addElement(es);
            }
            try {
                for (ExternalEngineManager.Xei xei : ExternalEngineManager.getXei()) {
                    final ExternalEngineSelector selector = new ExternalEngineSelector(xei.name, xei.wd, xei.cmd);
                    addElement(selector);
                }
            } catch (BackingStoreException e) {
                JOptionPane.showMessageDialog(null, "External engine preferences are unavailable");
            }
        }

        public void put(EngineSelector engineSelector) {
            final int i = find(engineSelector.name);
            if (i < 0) {
                addElement(engineSelector);
            } else {
                set(i, engineSelector);
            }
        }

        /**
         * @param name name of element to find
         * @return index of the first element whose name equals name, or -1 if no match found
         */
        private int find(String name) {
            for (int i = 0; i < size(); i++) {
                if (get(i).name.equals(name)) {
                    return i;
                }
            }
            return -1;
        }

    }

    /**
     * Nuke engine selectors
     */
    public static void main(String[] args) throws BackingStoreException {
        ExternalEngineManager.removeAll();
    }
}
