package com.datarecorder;

import java.awt.Container;
import java.awt.Dimension;
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
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;

import com.datarecorder.util.ui.DialogTitlePane;
import com.datarecorder.util.ui.EscapeDialog;
import com.datarecorder.util.ui.MessageBox;

public class ChannelViewer extends EscapeDialog {

    private static final long serialVersionUID = 893797379084830832L;
    private static final String DETAILS = DataRecorder.bundle.getString("channelViewer.Details");
    public static final String FILTER = "viewerType";
    private JButton clearButton = new JButton();
    private JButton closeButton = new JButton();
    private JComboBox selection;
    private int minWidth;
    private int minHeight;
    private Viewer myViewer;
    private JPanel defaultPanel;
    private Properties properties;
    private JCheckBox freezeBox;
    private JCheckBox autoDetect;
    private JLabel selectionLabel;
    private MyComboBoxModel comboBoxModel;
    private ImageIcon icon;
    private DialogTitlePane titlePane;
    private PropertyType defaultViewer;

    private Logger logger = LoggingObject.getLogger(this.getClass());

	public ChannelViewer(final DataRecorder parent) {
        super(parent, "", false);
        icon = Util.getImageFromResource(DataRecorder.bundle, "viewerButton.Icon");

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
        // View Control Panel
        final JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Viewer Selection"));

        comboBoxModel = new MyComboBoxModel();
        comboBoxModel.updateList(getListData());

        selection = new JComboBox(comboBoxModel);

        selection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                selectViewer();
            }
        });

        autoDetect = new JCheckBox("Auto-detect type");
        
        // this does not work (mwt)
        autoDetect.setEnabled(false);

        autoDetect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                autoDetect();
            }
        });
        selectionLabel = new JLabel("View As:");
        controlPanel.add(autoDetect, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        controlPanel.add(new JLabel(""), new GridBagConstraints(1, 0, 1, 1, 2.0, 2.0, GridBagConstraints.EAST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
        controlPanel.add(selectionLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        controlPanel.add(selection, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        // Channel Panel
        defaultPanel = new JPanel();
        defaultPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        // Button Panel
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        freezeBox = new JCheckBox("Freeze");
        freezeBox.setToolTipText(
                "Selecting freeze will halt the viewer at the last message received and messages sent to the viewer will resume with latest message");

        closeButton.setText("Close");
        clearButton.setText("Clear");
        freezeBox.setMargin(new Insets(0, 4, 0, 4));

        buttonPanel.add(freezeBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        buttonPanel.add(new JLabel(""), new GridBagConstraints(1, 0, 1, 1, 2.0, 2.0, GridBagConstraints.EAST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
        buttonPanel.add(clearButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        buttonPanel.add(closeButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        // Center Panel
        final Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        // Listeners
        closeButton.addActionListener(new CancelListener());
        clearButton.addActionListener(new ClearListener());
        addComponentListener(new MyComponentListener());

        // Finish
        final String title = "Viewer Tool";
        final String details = DETAILS;

        titlePane = new DialogTitlePane(title, details, icon);
        c.add(titlePane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
        c.add(controlPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
        c.add(defaultPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 2.0, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
        c.add(buttonPanel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));

        selectViewer();
        pack();
        minWidth = getWidth();
        minHeight = getHeight();
        setSize(new Dimension(600, 400));
        // set default to be auto detecting
        autoDetect.setSelected(true);
        autoDetect();
    }

    private void autoDetect() {
        if (autoDetect.isSelected()) {
            selection.setEnabled(false);
            selectionLabel.setEnabled(false);
        } else {
            selection.setEnabled(true);
            selectionLabel.setEnabled(true);
        }
    }

    private void selectViewer() {
        final PropertyType value = (PropertyType) selection.getSelectedItem();
        // String className = properties.getProperty(value);
        if (value != null && (myViewer == null || !myViewer.getClass().getName().equals(value.getClassName()))) {
            try {
                final Class clazz = Class.forName(value.getClassName());
                final Object object = clazz.newInstance();
                if (!Viewer.class.isAssignableFrom(object.getClass()))
                    MessageBox.showError(this,
                            "Plug-In class [" + value.getClassName() + "] is not derived from the Viewer class.");
                else {

                    myViewer = (Viewer) object;
                    defaultPanel.removeAll();
                    defaultPanel.invalidate();
                    defaultPanel.setLayout(new GridBagLayout());
                    defaultPanel.add(myViewer.getJPanel(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
                    defaultPanel.revalidate();
                }
            } catch (final ClassNotFoundException ex) {
                MessageBox.showWarning(this, "Unable to find class [" + ex.getMessage()
                        + "] while building Viewer Tool.\nCheck plug-ins under Options->Viewer Plug-Ins");
                logger.error("Unable to find class [" + ex.getMessage() + "] while building Viewer Tool.", ex);
            } catch (final InstantiationException ex) {
                MessageBox.showWarning(this, "Unable to instantiate class [" + ex.getMessage() + "]");
                logger.error("Unable to instantiate class [" + ex.getMessage() + "] while building Viewer Tool.", ex);
            } catch (final IllegalAccessException ex) {
                MessageBox.showWarning(this, "Unable to create class [" + ex.getMessage() + "]");
                logger.error("Unable to create class [" + ex.getMessage() + "] while building Viewer Tool.", ex);
            }
        }
    }

    private void cancel() {
        this.dispose();
    }

    private class MyComponentListener implements ComponentListener {
        @Override
        public void componentHidden(final ComponentEvent c) {
        }

        @Override
        public void componentShown(final ComponentEvent c) {
        }

        @Override
        public void componentMoved(final ComponentEvent c) {
        }

        @Override
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

    private class CancelListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent event) {
            cancel();
        }
    }

    private class ClearListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent event) {
            // Validate
            if (myViewer != null) {
                myViewer.clear();
            }
        }
    }

    public void updateList() {
        comboBoxModel.updateList(getListData());
    }

    private Vector<PropertyType> getListData() {
        // read bundle
        properties = DataRecorder.getPropertiesFile();

        final Enumeration<Object> enumerate = properties.keys();
        final Vector<PropertyType> list = new Vector<PropertyType>();
        while (enumerate.hasMoreElements()) {
            final String typeName = (String) enumerate.nextElement();
            if (typeName.startsWith(FILTER + ".")) {
                final String className = properties.getProperty(typeName);
                final PropertyType typeObject = new PropertyType(typeName.substring(FILTER.length() + 1), className);
                // removed this... the whole image/html thing does not work (mwt)
                //list.addElement(typeObject);
                if (typeObject.getTypeName().equals("text")) {
                	list.addElement(typeObject);
                    defaultViewer = typeObject;
                }
            }
        }
        return list;
    }

    /**
     * @param dataRecorderMessage
     */
    public void displayInformationObject(final DataRecorderMessage dataRecorderMessage) {
        if (myViewer != null && !freezeBox.isSelected()) {
            if (autoDetect.isSelected()) {
                selection.setSelectedItem(defaultViewer);
            }
            try {
                myViewer.displayInformationObject(dataRecorderMessage);
            } catch (final InvalidViewerMessageException ex) {
                MessageBox.showError(this, ex.getMessage());
                freezeBox.setSelected(true);
            }
        }
    }

 
    public void closed() {
        if (myViewer != null && !freezeBox.isSelected())
            myViewer.closed();

    }

    @Override
    public void show() {
        updateList();
        super.show();
        if (comboBoxModel.getSize() == 0)
            MessageBox.showWarning(this,
                    "No Viewer Plug-Ins have been defined.\nSpecify under the menu Options->Viewer Plug-Ins.");
    }

    static class MyComboBoxModel extends DefaultComboBoxModel {

        private static final long serialVersionUID = 8804674277543139410L;

        public void updateList(final Vector<PropertyType> vector) {
            this.removeAllElements();
            for (int i = 0; i < vector.size(); i++)
                this.addElement(vector.elementAt(i));
            this.fireContentsChanged(this, 0, vector.size());
        }

    }
}
