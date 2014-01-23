package com.welty.othello.gui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

import static com.orbanova.common.jsb.JSwingBuilder.frame;
import static com.orbanova.common.jsb.JSwingBuilder.grid;

/**
 */
public class SelectOpponentWindow {

    private static JFrame instance;

    public synchronized static JFrame getInstance() {
        if (instance == null) {
            // Level selection list box.
            // Need to create this before Opponent selection list box because the
            // Opponent selection list box modifies it.
            final JList<Integer> levels = new JList<>();
            final DefaultListModel<Integer> levelModel = new DefaultListModel<>();
            setLevelElements(levelModel, Opponent.advancedLevels);
            levels.setModel(levelModel);
            setUpList(levels);
            levels.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            levels.setVisibleRowCount(Opponent.advancedLevels.length/2);

            // Opponent selection list box.
            final JList<Opponent> ops = new JList<>(opponents);
            ops.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        final int index = ops.getSelectedIndex();
                        final Opponent opponent = opponents[index];
                        setLevelElements(levelModel, opponent.availableLevels);
                        levels.setSelectedIndex(findNearestLevel(13, opponent.availableLevels));
                    }
                }
            });
            setUpList(ops);



            instance = frame("Select Opponent", JFrame.HIDE_ON_CLOSE,
                    grid(2, 0, -1,
                            new JLabel("Opponent"), new JLabel("Level"),
                            ops, levels
                    ).spacing(5).border(10)
            );
        }
        return instance;
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
     *
     * This implementation assumes the levels are in order.
     *
     * @param targetLevel desired search depth
     * @param levels available search depth
     * @return index of search depth
     */
    private static int findNearestLevel(int targetLevel, Integer[] levels) {
        int i;
        for (i=0; i<levels.length; i++) {
            if (levels[i] > targetLevel) {
                break;
            }
        }
        if (i>0) {
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

    private static class Opponent {
        final String name;
        final Integer[] availableLevels;

        static final Integer[] basicLevels = {1, 2};
        static final Integer[] advancedLevels = {1, 2, 3, 4, 5, 6, 8, 10, 12, 16, 20, 24};

        private Opponent(String name, boolean isAdvanced) {
            this.name = name;
            this.availableLevels = isAdvanced?advancedLevels: basicLevels;
        }

        @Override public String toString() {
            // add spaces on either side so it looks a little nicer in the JList
            return " " + name + " ";
        }
    }

    private static final Opponent[] opponents = {
            new Opponent("Abigail", false),
            new Opponent("Carla", false),
            new Opponent("Edwina", false),
            new Opponent("George", false),
            new Opponent("Edax", true),
            new Opponent("NTest", true),
            new Opponent("Vegtbl", true)
    };
}
