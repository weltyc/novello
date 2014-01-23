package com.welty.othello.gui;

import javax.swing.*;
import java.awt.*;

import static com.orbanova.common.jsb.JSwingBuilder.frame;
import static com.orbanova.common.jsb.JSwingBuilder.grid;

/**
 */
public class SelectOpponentWindow {

    private static JFrame instance;

    public synchronized static JFrame getInstance() {
        if (instance == null) {
            final String[] opponents = {
                    "Abigail",//"Andy",
                    "Carla",// "Charles",
                    "Edwina",// "Ethelred",
                    "George", //"Grandma",
                    "Edax", "NTest", "Vegtbl"
            };
            final JList<String> ops = new JList<>(opponents);
            setUpList(ops);

            final JList<Integer> levels = new JList<>(new Integer[]{1, 2, 3, 4, 5, 6, 8, 10, 12, 16, 20, 24});
            setUpList(levels);


            instance = frame("Select Opponent", JFrame.HIDE_ON_CLOSE,
                    grid(2, 0, -1,
                            new JLabel("Opponent"), new JLabel("Level"),
                            ops, levels
                    ).spacing(5).border(10)
            );
        }
        return instance;
    }

    private static <T> void setUpList(JList<T> ops) {
        ops.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        ops.setFont(UIManager.getFont("TextField.font"));
        ops.setAlignmentY(0.0f);
        ops.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ops.setSelectedIndex(0);
    }
}
