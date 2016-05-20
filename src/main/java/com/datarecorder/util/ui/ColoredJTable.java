/*
 * Created on Nov 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.datarecorder.util.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class ColoredJTable extends JTable {

    private static final long serialVersionUID = -8717800465792351211L;
    private Color _color = new Color(220, 220, 220);

    /**
     * 
     */
    public ColoredJTable() {
        super();
    }

    /**
     * @param numRows
     * @param numColumns
     */
    public ColoredJTable(final int numRows, final int numColumns) {
        super(numRows, numColumns);
    }

    /**
     * @param rowData
     * @param columnNames
     */
    public ColoredJTable(final Object[][] rowData, final Object[] columnNames) {
        super(rowData, columnNames);
    }

    /**
     * @param rowData
     * @param columnNames
     */
    public ColoredJTable(final Vector<Object> rowData, final Vector<Object> columnNames) {
        super(rowData, columnNames);
    }

    /**
     * @param dm
     * @param cm
     */
    public ColoredJTable(final TableModel dm, final TableColumnModel cm) {
        super(dm, cm);
    }

    /**
     * @param dm
     * @param cm
     * @param sm
     */
    public ColoredJTable(final TableModel dm, final TableColumnModel cm, final ListSelectionModel sm) {
        super(dm, cm, sm);
    }

    public ColoredJTable(final TableModel tm) {
        super(tm);
    }

    public void setAlternatingRowColor(final Color color) {
        _color = color;
    }

    @Override
    public Component prepareRenderer(final TableCellRenderer renderer, final int rowIndex, final int vColIndex) {
        final Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
        if ((rowIndex & 1) == 1 && !isCellSelected(rowIndex, vColIndex)) {
            c.setBackground(_color);
        } else {
            if (!isCellSelected(rowIndex, vColIndex)) {
                // If not shaded, match the table's background
                c.setBackground(getBackground());
            } else {
                c.setBackground(this.getSelectionBackground());
            }
        }
        return c;
    }
}
