package com.datarecorder;

import java.awt.BorderLayout;
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.datarecorder.util.ui.WindowUtils;

public class PropertyDialog extends JDialog {

    private static final long serialVersionUID = 583910039994007587L;
    public static final String ADD_MODE = "ADD";
    public static final String EDIT_MODE = "EDIT";
    private static final String CANCEL = "CANCEL";
    private static final String ADD = "ADD";
    private static final String APPLY = "APPLY";

    // private DataRecorder dataRecorder;
    private JComboBox type = new JComboBox();
    private JTextField className = new JTextField(30);
    private JButton actionButton = new JButton();
    private JButton cancelButton = new JButton();
    private String mode;
    private String action;
    private int minWidth;
    private int minHeight;

    public PropertyDialog(final PropertyEditor parent) {

        super(parent);
        // Set element attributes
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                cancel();
            }
        });

        setModal(true);
        setResizable(true);
        type.setEditable(false);
        className.setEditable(false);

        // Channel Panel
        final JPanel channelPanel = new JPanel(new GridBagLayout());
        channelPanel.setBorder(BorderFactory.createTitledBorder("Specify Plug-in"));
        channelPanel.add(new JLabel("Type:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        channelPanel.add(new JLabel("Plug-in:"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        channelPanel.add(type, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 0, 4, 4), 0, 0));

        channelPanel.add(className, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 0, 4, 4), 0, 0));

        // Button Panel
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton.setText("Cancel");
        cancelButton.setMargin(new Insets(0, 4, 0, 4));
        actionButton.setMargin(new Insets(0, 4, 0, 4));
        buttonPanel.add(actionButton);
        buttonPanel.add(cancelButton);

        // Center Panel
        final Container c = getContentPane();
        c.setLayout(new BorderLayout());
        final JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(8, 8, 0, 8)));
        centerPanel.add(channelPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0));
        centerPanel.add(new JPanel(), new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_START,
                GridBagConstraints.BOTH, new Insets(0, 0, 4, 0), 0, 0));

        // Listeners
        cancelButton.addActionListener(new CancelListener());
        actionButton.addActionListener(new MyActionListener());
        addComponentListener(new MyComponentListener());

        // Finish
        c.add(centerPanel, BorderLayout.CENTER);
        c.add(buttonPanel, BorderLayout.SOUTH);
        pack();
        minWidth = getWidth();
        minHeight = getHeight();
        // myRef = this;
    }

    public void setVisible(final boolean state, final String mode, final int row) {
        this.mode = mode;
        // show
        if (state) {
            if (mode.equals(ADD_MODE)) {
                setTitle(" Add Plug-in");
                actionButton.setText("Add");
            } else {
                if (mode.equals(EDIT_MODE)) {
                    setTitle(" Edit Plug-in");
                    actionButton.setText("Apply");
                }
            }
            WindowUtils.centerWindow(this);
        }

        super.setVisible(state);
    }

    public boolean isAction() {
        if (action.equals(CANCEL)) {
            return false;
        } else {
            return true;
        }
    }

    // add get type and get className
    private void cancel() {
        // Check if things have changed
        action = CANCEL;
        this.setVisible(false);
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

    private class MyActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            // Validate
            if (type.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(PropertyDialog.this, "A type must be selected", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!className.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(PropertyDialog.this, "A class name for the plug-in must be entered",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (mode.equals(ADD_MODE)) {
                action = ADD;
            }

            if (mode.equals(EDIT_MODE)) {
                action = APPLY;
            }

            setVisible(false);
        }
    }
}
