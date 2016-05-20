package com.datarecorder;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.datarecorder.util.ui.ColoredJTable;
import com.datarecorder.util.ui.DialogTitlePane;
import com.datarecorder.util.ui.EscapeDialog;
import com.datarecorder.util.ui.MessageBox;
import com.datarecorder.util.ui.WindowUtils;

public class PropertyEditor extends EscapeDialog {

    private static final long serialVersionUID = 3543133872973934627L;
    private String INJECTOR_TITLE = DataRecorder.bundle.getString("propertyInjector.Title");
    private String INJECTOR_DETAIL = DataRecorder.bundle.getString("propertyInjector.Details");
    private String VIEWER_TITLE = DataRecorder.bundle.getString("propertyViewer.Title");
    private String VIEWER_DETAIL = DataRecorder.bundle.getString("propertyViewer.Details");

    private JButton okButton = new JButton();
    private JButton cancelButton = new JButton();
    private int minWidth;
    private int minHeight;
    // private JPanel defaultPanel;
    private JTable table;
    private PropertyTypeModel propertyModel;
    private JScrollPane scroller;

    private Class instanceClass;
    private String filter;

    private JButton editButton;
    private JButton addButton;
    private JButton deleteButton;
    private JButton verifyButton;

    private PlugInDialog pdialog;
    private Properties propertyFile;
    private DataRecorder parent;

    private DialogTitlePane titlePane;

    public PropertyEditor(final DataRecorder parent, final Class instanceClass, final String filter) {
        super(parent, "Property Editor", true);
        this.parent = parent;

        propertyFile = DataRecorder.getPropertiesFile();
        this.filter = filter;
        this.instanceClass = instanceClass;

        // read in initial types from file
        final Enumeration<Object> enumerate = propertyFile.keys();
        final Vector<PropertyType> typeList = new Vector<PropertyType>();
        while (enumerate.hasMoreElements()) {
            final String typeName = (String) enumerate.nextElement();
            if (typeName.startsWith(filter + ".")) {
                final String className = propertyFile.getProperty(typeName);
                final PropertyType typeObject = new PropertyType(typeName.substring(filter.length() + 1), className);
                typeList.addElement(typeObject);
            }
        }
        final PropertyType[] types = new PropertyType[typeList.size()];
        System.arraycopy(typeList.toArray(), 0, types, 0, types.length);

        propertyModel = new PropertyTypeModel(types);

        addButton = new JButton("Add...");
        editButton = new JButton("Edit...");
        deleteButton = new JButton("Delete");
        verifyButton = new JButton("Verify");

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                add();
            }
        });
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                edit();
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                delete();
            }
        });
        verifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                verify();
            }
        });

        // this.setJMenuBar(menu);
        table = new ColoredJTable(propertyModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        scroller = new JScrollPane(table);
        // Set element attributes
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                cancel();
            }
        });

        setModal(false);
        setResizable(true);

        // Button Panel
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        cancelButton.setText("Cancel");
        okButton.setText("OK");
        getRootPane().setDefaultButton(okButton);
        buttonPanel.add(new JLabel(""), new GridBagConstraints(1, 0, 1, 1, 2.0, 2.0, GridBagConstraints.EAST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
        buttonPanel.add(okButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        buttonPanel.add(cancelButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        // Center Panel
        final Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        // Listeners
        cancelButton.addActionListener(new CancelListener());
        okButton.addActionListener(new OkListener());
        addComponentListener(new MyComponentListener());

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(final ListSelectionEvent e) {
                if (table.getSelectedRowCount() == 0) {
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    verifyButton.setEnabled(false);
                } else if (table.getSelectedRowCount() == 1) {
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    verifyButton.setEnabled(true);
                } else {
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(true);
                    verifyButton.setEnabled(true);
                }
            }
        });

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        verifyButton.setEnabled(false);

        final JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(verifyButton);

        // View Control Panel
        final JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());

        controlPanel.setBorder(BorderFactory.createLoweredBevelBorder());

        controlPanel.add(scroller, new GridBagConstraints(0, 0, 1, 1, 2.0, 2.0, GridBagConstraints.EAST,
                GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));

        controlPanel.add(actionPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        // Finish
        String label = null;
        String details = null;
        ImageIcon icon = null;
        if (instanceClass.getName().equals(Viewer.class.getName())) {
            label = VIEWER_TITLE;
            details = VIEWER_DETAIL;
            // Details
            icon = Util.getImage("/com/tools/common/resources/Details24.gif");

        } else if (instanceClass.getName().equals(Injector.class.getName())) {
            // Details
            label = INJECTOR_TITLE;
            details = INJECTOR_DETAIL;
            icon = Util.getImage("/com/tools/common/resources/Import24.gif");

        }

        titlePane = new DialogTitlePane(label, details, icon);
        c.add(titlePane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        c.add(controlPanel, new GridBagConstraints(0, 1, 1, 1, 2.0, 5.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 4, 0), 0, 0));
        c.add(buttonPanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));

        pack();
        minWidth = getWidth();
        minHeight = getHeight();
        WindowUtils.centerWindow(this, getParent());
    }

    /**
     * 
     */
    private void add() {
        getDialog().setTitle("Add Plug-in");
        getDialog().setTypeName("");
        getDialog().setClassName("");
        WindowUtils.centerWindow(getDialog(), this);
        getDialog().show();
        while (propertyModel.contains(pdialog.getTypeName()) && getDialog().getAction() == PlugInDialog.OK) {
            MessageBox.showWarning(this, "Type name already exists in the list.");
            getDialog().show();
        }
        if (getDialog().getAction() == PlugInDialog.OK) {
            final PropertyType type = new PropertyType(pdialog.getTypeName(), pdialog.getClassName());
            final Vector<PropertyType> typeList = new Vector<PropertyType>();
            typeList.add(type);
            final PropertyType[] types = new PropertyType[typeList.size()];
            System.arraycopy(typeList.toArray(), 0, types, 0, types.length);

            propertyModel.addRow(type);

        }
    }

    private void delete() {
        if (table.getCellEditor() != null)
            table.getCellEditor().stopCellEditing();

        final int value = MessageBox.showConfirm(this, "Are you sure you wish to delete the selection(s)");
        if (value == JOptionPane.YES_OPTION) {
            propertyModel.removeRows(table.getSelectedRows());
        }
    }

    private void edit() {
        if (table.getCellEditor() != null)
            table.getCellEditor().stopCellEditing();

        getDialog().setTitle("Edit Plug-in");
        final PropertyType type = propertyModel.getRow(table.getSelectedRow());

        getDialog().setTypeName(type.getTypeName()); // get row
        getDialog().setClassName(type.getClassName()); //

        getDialog().show();

        type.setTypeName(getDialog().getTypeName());
        type.setClassName(getDialog().getClassName());
        propertyModel.setRow(table.getSelectedRow(), type);
    }

    private void verify() {
        if (table.getCellEditor() != null)
            table.getCellEditor().stopCellEditing();

        final int rows[] = table.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            final PropertyType type = propertyModel.getRow(rows[i]);
            try {
                final Class cls = Class.forName(type.getClassName());
                if (!instanceClass.isAssignableFrom(cls)) {
                    MessageBox.showWarning(this, "Class [" + type.getClassName() + "] does not inherit from class ["
                            + instanceClass.getName() + "]");
                } else
                    MessageBox.showInfo(this, "Class is valid.");
            } catch (final ClassNotFoundException e) {
                MessageBox.showWarning(this, "Class not found in classpath [" + type.getClassName() + "]");
            }
        }
    }

    private void ok() {
        // update then hide();
        // validate
        table.getSelectionModel().clearSelection();
        // allows last entry to be entered
        if (table.getCellEditor() != null)
            table.getCellEditor().stopCellEditing();

        // remove old elements
        final Enumeration<Object> enumerate = propertyFile.keys();
        while (enumerate.hasMoreElements()) {
            final String typeName = (String) enumerate.nextElement();
            if (typeName.startsWith(filter + "."))
                propertyFile.remove(typeName);
        }

        final Vector<PropertyType> data = propertyModel.getDataVector();
        final Enumeration<PropertyType> iter = data.elements();
        while (iter.hasMoreElements()) {
            final PropertyType type = iter.nextElement();
            propertyFile.put(filter + "." + type.getTypeName(), type.getClassName());

        }
        Util.saveProperties(this, propertyFile, DataRecorder.PROPERTY_FILE);

        // update other viewers and injectors
        update();
        this.dispose();
    }

    private void update() {
        for (int i = 0; i < parent.publisherModel.getRowCount(); i++) {
            final Vector<ChannelViewer> v = (Vector<ChannelViewer>) parent.publisherModel.getDataVector().elementAt(i);
            final ChannelViewer cv = v.get(PublisherTableValues.CHANNEL_VIEWER);
            cv.updateList();
        }
        for (int i = 0; i < parent.subscriberModel.getRowCount(); i++) {

            final Vector<ChannelViewer> v = (Vector<ChannelViewer>) parent.subscriberModel.getDataVector().elementAt(i);
            final ChannelViewer cv = v.get(SubscriberTableValues.CHANNEL_VIEWER);
            cv.updateList();
        }
    }

    private void cancel() {
        // don't make any changes
        this.setVisible(false);
    }

    private PlugInDialog getDialog() {
        if (pdialog == null)
            pdialog = new PlugInDialog((JFrame) this.getParent());
        return pdialog;
    }

    private class MyComponentListener implements ComponentListener {
        public void componentHidden(final ComponentEvent c) {
        }

        public void componentShown(final ComponentEvent c) {
        }

        public void componentMoved(final ComponentEvent c) {
        }

        public void componentResized(final ComponentEvent c) {
            final JDialog source = (JDialog) c.getSource();
            int width = source.getWidth();
            int height = source.getHeight();
            if (width < minWidth)
                width = minWidth;
            if (height < minHeight)
                height = minHeight;
            source.setSize(width, height);
        }
    }

    private class OkListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            ok();
        }
    }

    private class CancelListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            cancel();
        }
    }

    @Override
    public void show() {
        if (instanceClass.getName().equals(Viewer.class.getName()))
            titlePane.setDetails(VIEWER_DETAIL);
        else if (instanceClass.getName().equals(Injector.class.getName()))
            titlePane.setDetails(INJECTOR_DETAIL);

        super.show();
    }

    public static void main(final String[] args) {
        // WindowUtils.setNativeLookAndFeel();
        // PropertyEditor viewer = new PropertyEditor(new JFrame(), com.tools.dr2.Viewer.class,
        // "datatype");
        // viewer.show();
    }

}
