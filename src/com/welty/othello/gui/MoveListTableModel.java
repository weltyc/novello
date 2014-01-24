package com.welty.othello.gui;

import com.orbanova.common.misc.Utils;
import com.welty.novello.core.BitBoardUtils;
import com.welty.novello.core.Move8x8;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;
import java.util.List;

/**
 */
class MoveListTableModel extends AbstractTableModel implements GameModel.ChangeListener {
    private final GameModel gameModel;
    private List<Move8x8> moves;

    static JScrollPane of(GameModel gameModel) {
        final MoveListTableModel model = new MoveListTableModel(gameModel);

        final JTable jTable = new MyTable(model, gameModel);
        final JScrollPane scrollPane = new JScrollPane(jTable);

        // set size
        final int width = 200;
        final Dimension dim = new Dimension(width, 0);
        scrollPane.setPreferredSize(dim);
        scrollPane.setMinimumSize(dim);
        scrollPane.setMaximumSize(new Dimension(width, 10000));

        return scrollPane;
    }

    private MoveListTableModel(GameModel gameModel) {
        this.gameModel = gameModel;
        gameModel.addChangeListener(this);
        moves = gameModel.getMoves();
    }

    @Override public int getRowCount() {
        final int n = moves.size();
        if (n == 0) {
            return 0;
        }
        int extra = extra();
        return (n + extra + 1) / 2;
    }

    private int extra() {
        return gameModel.getStartPosition().blackToMove ? 0 : 1;
    }

    @Override public int getColumnCount() {
        return 4;
    }

    private static final String[] columnNames = {"Black", "Score", "White", "Score"};

    @Override public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override public Class<?> getColumnClass(int columnIndex) {
        return Utils.isOdd(columnIndex) ? Double.class : String.class;
    }

    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        final int moveNumber = moveNumber(rowIndex, columnIndex);
        if (moveNumber >= 0) {
            final Move8x8 move = moves.get(moveNumber);
            if (Utils.isOdd(columnIndex)) {
                return move.eval;
            } else {
                if (move.getSq() >= 0) {
                    return " " + BitBoardUtils.sqToText(move.getSq());
                } else {
                    return " pass";
                }
            }
        } else {
            return null;
        }
    }

    /**
     * @return the index of the corresponding move in the moves list, or -1 if there is no corresponding move
     */
    private int moveNumber(int rowIndex, int columnIndex) {
        final int moveNumber = rowIndex * 2 + columnIndex / 2 - extra();
        return moveNumber >= moves.size() ? -1 : moveNumber;
    }

    @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new IllegalStateException("not implemented");
    }

    @Override public void gameViewChanged() {
        moves = gameModel.getMoves();
        fireTableDataChanged();
    }

    private static class MyMouseAdapter extends MouseAdapter {
        private final MoveListTableModel model;
        private final JTable jTable;

        public MyMouseAdapter(MoveListTableModel model, JTable jTable) {
            this.model = model;
            this.jTable = jTable;
        }

        @Override public void mousePressed(MouseEvent e) {
            final int row = jTable.rowAtPoint(e.getPoint());
            final int col = jTable.columnAtPoint(e.getPoint());
            if (row >= 0 && col >= 0) {
                final int move = model.moveNumber(row, col);
                if (move >= 0) {
                    model.gameModel.setIPosition(move);
                }
            }
        }
    }

    private static class EvalRenderer extends DefaultTableCellRenderer {
        private final DecimalFormat numberFormat = new DecimalFormat("#.0");

        @Override protected void setValue(Object value) {
            if (value == null) {
                super.setValue("");
                return;
            }
            final double v = (Double) value;
            if (v == 0) {
                super.setValue("");
            } else {
                super.setValue(numberFormat.format(v));
            }
            setHorizontalAlignment(JLabel.RIGHT);
            setForeground(v < 0 ? Color.RED : Color.BLACK);
        }
    }

    private static class MyTable extends JTable implements GameModel.ChangeListener {
        private final MoveListTableModel model;
        private final GameModel gameModel;

        public MyTable(MoveListTableModel model, GameModel gameModel) {
            super(model);
            this.model = model;
            this.gameModel = gameModel;
            final JTableHeader header = getTableHeader();
            header.setReorderingAllowed(false);
            header.setResizingAllowed(false);
            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            setRowSelectionAllowed(true);
            setColumnSelectionAllowed(true);
            setDefaultRenderer(Double.class, new EvalRenderer());

            // disable mouse selection
            for (MouseListener l : getMouseListeners()) {
                removeMouseListener(l);
            }
            for (MouseMotionListener l : getMouseMotionListeners()) {
                removeMouseMotionListener(l);
            }

            // add my own mouse listener
            addMouseListener(new MyMouseAdapter(model, this));

            gameModel.addChangeListener(this);
            gameViewChanged();
        }

        @Override public void gameViewChanged() {
            final int move = gameModel.getIPosition();
            final boolean shouldHighlightMove = move < gameModel.nMoves();
            final int scrollTo = Math.min(move, gameModel.nMoves() - 1) + model.extra();
            final int row = scrollTo / 2;
            final int col = scrollTo & 1;
            if (move > 0 || shouldHighlightMove) {
                scrollToVisible(row, col);
            }

            if (shouldHighlightMove) {
                setRowSelectionInterval(row, row);
                setColumnSelectionInterval(col * 2, col * 2 + 1);
            } else {
                clearSelection();
            }
            repaint();
        }

        public void scrollToVisible(int row, int col) {
            if (!SwingUtilities.isEventDispatchThread()) {
                throw new RuntimeException("This must be called from the Event Dispatch Thread");
            }
            scrollRectToVisible(new Rectangle(getCellRect(row, col, true)));
        }
    }
}
