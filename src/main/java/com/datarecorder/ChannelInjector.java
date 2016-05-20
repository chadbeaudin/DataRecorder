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

public class ChannelInjector extends EscapeDialog {

    private static final long serialVersionUID = 4760419892973950314L;
    public static final String FILTER = "injectorType";
    private static final String DETAILS = DataRecorder.bundle.getString("channelInjector.Details");
    // private static final String EMPTY_STRING = "";
    private DialogTitlePane titlePane;
    private JButton clearButton = new JButton();
    private JButton closeButton = new JButton();
    private JComboBox selection;
    private int minWidth;
    private int minHeight;
    private Injector myInjector;
    private JPanel defaultPanel;
    // private Properties properties;
    // private JCheckBox freezeBox;
    private JCheckBox autoDetect;
    private JLabel selectionLabel;
    private Publisher publisher;
    private MyComboBoxModel comboBoxModel;
    private ImageIcon icon;
    private PropertyType defaultViewer;
    private PropertyType lastViewer;
    private ActionListener listener;

    private Logger logger = LoggingObject.getLogger(this.getClass());

    public ChannelInjector(final DataRecorder parent, final Publisher publisher) {
        super(parent, "", false);
        this.publisher = publisher;

        // properties = DataRecorder.getPropertiesFile();
        icon = Util.getImageFromResource(DataRecorder.bundle, "injectorButton.Icon");

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
        controlPanel.setBorder(BorderFactory.createTitledBorder("Injector Selection"));

        comboBoxModel = new MyComboBoxModel();
        updateList();
        selection = new JComboBox(comboBoxModel);
        selection.setSelectedItem(defaultViewer);

        listener = new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                selectViewer();
            }
        };
        selection.addActionListener(listener);

        autoDetect = new JCheckBox("Auto-detect type");
        autoDetect.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                autoDetect();
            }
        });
        selectionLabel = new JLabel("Inject As:");
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

        closeButton.setText("Close");
        clearButton.setText("Clear");
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
        final String title = "Injector Tool";
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

        autoDetect.setSelected(true);
        autoDetect();
        pack();
        minWidth = getWidth();
        minHeight = getHeight();
        setSize(new Dimension(600, 400));

    }

    private void autoDetect() {
        if (autoDetect.isSelected()) {
            selection.setEnabled(false);
            selectionLabel.setEnabled(false);
            final String name = publisher.getPublicationTypeName();
            for (int i = 0; i < selection.getItemCount(); i++) {
                if (name.equals(((PropertyType) selection.getItemAt(i)).getTypeName())) {
                    selection.setSelectedIndex(i);
                    break;
                } else {
                    selection.setSelectedItem(defaultViewer);
                }
            }
        } else {
            selection.setEnabled(true);
            selectionLabel.setEnabled(true);
        }
    }

    private void selectViewer() {
        final PropertyType value = (PropertyType) selection.getSelectedItem();
        if (value != null && (myInjector == null || !myInjector.getClass().getName().equals(value.getClassName()))) {
            try {
                final Class clazz = Class.forName(value.getClassName());
                final Object object = clazz.newInstance();
                if (!Injector.class.isAssignableFrom(object.getClass()))
                    MessageBox.showError(this,
                            "Plug-In class [" + value.getClassName() + "] is not derived from the Injector class.");
                else {
                    myInjector = (Injector) object;
                    myInjector.setPublisher(publisher);
                    defaultPanel.removeAll();
                    defaultPanel.invalidate();
                    defaultPanel.setLayout(new GridBagLayout());
                    defaultPanel.add(myInjector.getJComponent(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
                    defaultPanel.revalidate();
                }
            } catch (final ClassNotFoundException ex) {
                MessageBox.showWarning(this, "Unable to find class [" + ex.getMessage()
                        + "] while building Injector.\nCheck plug-ins under Options->Injector Plug-Ins");
                logger.error("Unable to find class [" + ex.getMessage() + "] while building Injector.", ex);
            } catch (final InstantiationException ex) {
                MessageBox.showWarning(this, "Unable to instantiate class [" + ex.getMessage() + "]");
                logger.error("Unable to instantiate class [" + ex.getMessage() + "]", ex);
            } catch (final IllegalAccessException ex) {
                MessageBox.showWarning(this, "Unable to create class [" + ex.getMessage() + "]");
                logger.error("Unable to create class [" + ex.getMessage() + "]", ex);
            }
        }
    }

    private void cancel() {
        lastViewer = (PropertyType) selection.getSelectedItem();
        this.dispose();
    }

    public void updateList() {
        comboBoxModel.updateList(getListData());
    }

    private Vector<Object> getListData() {
        // read bundle
        final Properties properties = DataRecorder.getPropertiesFile();

        final Enumeration<Object> enumerate = properties.keys();
        final Vector<Object> list = new Vector<Object>();
        while (enumerate.hasMoreElements()) {
            final String typeName = (String) enumerate.nextElement();
            if (typeName.startsWith(FILTER + ".")) {
                final String className = properties.getProperty(typeName);
                final PropertyType typeObject = new PropertyType(typeName.substring(FILTER.length() + 1), className);
                list.addElement(typeObject);

                if (typeObject.getTypeName().equals("text"))
                    defaultViewer = typeObject;
            }
        }
        return list;
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

    private class CancelListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            cancel();
        }
    }

    private class ClearListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            // Validate
            if (myInjector != null) {
                myInjector.clear();
            }
        }
    }

    @Override
    public void show() {
        // selection.removeActionListener(listener);
        updateList();
        if (lastViewer != null) {
            selection.setSelectedItem(lastViewer);
        }
        autoDetect();
        selectViewer();
        super.show();

        if (comboBoxModel.getSize() == 0)
            MessageBox.showWarning(this,
                    "No Injector Plug-Ins have been defined.\nSpecify under the menu Options->Injector Plug-Ins.");
    }

    public static void main(final String[] args) {
        // InjectorViewer viewer = new InjectorViewer(new JFrame(), null);
        // viewer.show();
    }

    class MyComboBoxModel extends DefaultComboBoxModel {

        private static final long serialVersionUID = 3256722896591599161L;

        public void updateList(final Vector<Object> data) {
            this.removeAllElements();
            for (int i = 0; i < data.size(); i++) {
                this.addElement(data.elementAt(i));
                if (lastViewer != null
                        && lastViewer.getTypeName().equals(((PropertyType) data.elementAt(i)).getTypeName()))
                    lastViewer = (PropertyType) data.elementAt(i);
            }
            this.fireContentsChanged(this, 0, data.size());
        }
    }
}
