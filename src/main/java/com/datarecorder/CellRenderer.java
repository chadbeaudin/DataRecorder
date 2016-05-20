package com.datarecorder;

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 5521621483529065863L;

    public CellRenderer() {
        setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(final JTable jTable, final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int col) {
        final Component c = super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, col);

        final JLabel label = (JLabel) c;

        // status
        if (col == 0) {
            if (label.getText().equals(PublisherTableValues.STOPPED)) {
                label.setForeground(Color.RED.darker());
            } else if (label.getText().equals(PublisherTableValues.STARTED)) {
                label.setForeground(Color.GREEN.darker());
            } else if (label.getText().equals(PublisherTableValues.PAUSED)) {
                label.setForeground(Color.YELLOW.darker());
            }
        } else {
            if (isSelected) {
                label.setForeground(Color.WHITE);
            } else {
                label.setForeground(Color.BLACK);
            }
        }

        // count
        if (col == 1) {
            final int v = Integer.parseInt(label.getText());
            label.setText(NumberFormat.getInstance().format(v));
        }

        // no border on focus or selected
        if ((hasFocus) || (isSelected)) {
            label.setBorder(super.noFocusBorder);
        }

        return c;
    }
}