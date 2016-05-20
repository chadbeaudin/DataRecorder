package com.datarecorder;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * @author nthrasher
 *
 */
public class PropertyTypeModel extends DefaultTableModel {

    private static final long serialVersionUID = -7217881222443972898L;
    private LinkedList<PropertyType> data = new LinkedList<PropertyType>();
    private String[] headers = { "Information Type", "Plug-in" };
    private Comparator<Object> comparator = new DataComparator();

    public PropertyTypeModel(final PropertyType[] data) {
        setDataVector(data);
        super.setColumnIdentifiers(headers);
    }

    public void setDataVector(final Object[] data) {
        if (data != null) {
            this.data.clear();
            for (int i = 0; i < data.length; i++) {
                this.data.add((PropertyType) data[i]);
            }
            Collections.sort(this.data, comparator);
        }
        super.fireTableDataChanged();

    }

    @Override
    public Vector<PropertyType> getDataVector() {
        final Vector<PropertyType> vector = new Vector<PropertyType>();
        final Iterator<PropertyType> iter = data.iterator();
        while (iter.hasNext()) {
            vector.add(iter.next());
        }
        return vector;
    }

    @Override
    public int getRowCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return headers.length;
    }

    public PropertyType getRow(final int row) {
        if (row > -1) {
            if (data.size() > row) {
                return data.get(row);
            }
        }
        return null;
    }

    public PropertyType[] getRows() {
        PropertyType[] configs = new PropertyType[data.size()];
        configs = data.toArray(configs);
        return configs;
    }

    @Override
    public boolean isCellEditable(final int row, final int col) {
        return true;
    }

    public void addRow(final Object newRow) {
        this.data.add((PropertyType) newRow);
        Collections.sort(data, comparator);
        super.fireTableRowsInserted(data.size(), (data.size()));
        super.fireTableDataChanged();
    }

    public boolean contains(final String key) {
        boolean response = false;
        for (int i = 0; i < this.data.size(); i++) {
            final PropertyType type = this.data.get(i);
            if (type.getTypeName().equals(key)) {
                response = true;
                break;
            }
        }
        return response;
    }

    @Override
    public void addRow(final Object[] newRows) {
        for (int i = 0; i < newRows.length; i++) {
            this.data.add((PropertyType) newRows[i]);
        }
        Collections.sort(data, comparator);
        super.fireTableRowsInserted(data.size(), (data.size() + newRows.length - 1));
    }

    public void setRow(final int row, final PropertyType type) {
        data.remove(row);
        data.add(row, type);
        super.fireTableDataChanged();

    }

    @Override
    public void removeRow(final int row) {
        data.remove(row);
        super.fireTableRowsDeleted(row, row);
        super.fireTableDataChanged();
    }

    public void removeRows(final int[] rows) {
        for (int i = rows.length - 1; i >= 0; i--)
            removeRow(rows[i]);
    }

    public void removeRows(final PropertyType[] deletions) {
        for (int i = 0; i < deletions.length; i++) {
            data.remove(deletions[i]);
        }
        super.fireTableDataChanged();
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(final int arg0, final int arg1) {

        if (data.get(arg0) == null) {
            return "";
        }

        final PropertyType temp = data.get(arg0);

        try {
            switch (arg1) {
            case 0:
                return temp.getTypeName();

            case 1:
                return temp.getClassName();
            }
        } catch (final NullPointerException npe) {
            return "";
        }
        return "";
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public void setValueAt(final Object value, final int arg0, final int arg1) {

        final PropertyType temp = data.get(arg0);

        switch (arg1) {
        case 0:
            temp.setTypeName((String) value);
            break;
        case 1:
            temp.setClassName((String) value);
            break;
        }

    }

    @Override
    public Class<String> getColumnClass(final int index) {
        return String.class;
    }
}

class DataComparator implements Comparator<Object>, Serializable {

    private static final long serialVersionUID = 3701366505767244775L;

    public int compare(final Object o1, final Object o2) {
        final PropertyType u1 = (PropertyType) o1;
        final PropertyType u2 = (PropertyType) o2;
        return u1.getTypeName().compareTo(u2.getTypeName());
    }
}
