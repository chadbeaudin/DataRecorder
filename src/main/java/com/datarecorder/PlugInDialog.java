package com.datarecorder;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.datarecorder.util.ui.EscapeDialog;
import com.datarecorder.util.ui.MessageBox;
import com.datarecorder.util.ui.WindowUtils;

/**
 * @author nthrasher
 */
public class PlugInDialog extends EscapeDialog implements ActionListener {

    private static final long serialVersionUID = -7895776406050810494L;
    private JButton ok = new JButton("OK");
    private JButton cancel = new JButton("Cancel");
    private JTextField typeName = new JTextField(20);
    private JTextField plugInClass = new JTextField(20);
    private int action;
    public static final int OK = 0;
    public static final int CLOSE = 1;

    /**
     * @param parent
     *            the parent frame for the dialog
     * @throws HeadlessException
     */
    public PlugInDialog(final JFrame parent) throws HeadlessException {
        super(parent, null, true);
        setModal(true);
        init();
        pack();
    }

    private void init() {
        final Container c = getContentPane();
        final JPanel top = new JPanel();
        setResizable(false);
        c.add(top);
        top.setLayout(new BorderLayout());
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        top.add(buttonPanel, BorderLayout.SOUTH);
        final JPanel contents = new JPanel();
        contents.setBorder(BorderFactory.createTitledBorder(this.getTitle()));
        contents.setLayout(new GridBagLayout());
        top.add(contents, BorderLayout.CENTER);

        contents.add(new JLabel("Type Name:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(6, 6, 6, 0), 0, 0));

        contents.add(new JLabel("Plug-in Class:"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(6, 6, 6, 0), 0, 0));

        contents.add(typeName, new GridBagConstraints(1, 0, 1, 1, 2.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(6, 6, 6, 6), 0, 0));
        contents.add(plugInClass, new GridBagConstraints(1, 1, 1, 1, 2.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(6, 6, 6, 6), 0, 0));

        // contents.add(new JLabel(),
        // new GridBagConstraints(0, 2, 1, 1, 1.0, 5.0
        // ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
        // new Insets(0, 6, 5, 0), 0, 0));
        getRootPane().setDefaultButton(ok);

        ok.addActionListener(this);
        getRootPane().setDefaultButton(ok);
        cancel.addActionListener(this);
    }

    public String getTypeName() {
        return typeName.getText().trim();
    }

    public String getClassName() {
        return plugInClass.getText().trim();
    }

    public void setTypeName(final String value) {
        typeName.setText(value);
    }

    public void setClassName(final String value) {
        plugInClass.setText(value);
    }

    private boolean validateData() {

        if (getTypeName().equals("")) {
            MessageBox.showWarning(this, "The type name must be specified.");
            return false;
        }
        if (getClassName().trim().equals("")) {
            MessageBox.showWarning(this, "The plug-in class name must be specified.");
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent arg0) {
        final Object source = arg0.getSource();
        if (source == ok) {
            action = OK;
            WindowUtils.setWaitCursor(this, true);
            if (validateData())
                dispose();
            WindowUtils.setWaitCursor(this, false);
        } else if (source == cancel) {
            action = CLOSE;
            dispose();
        }
    }

    public int getAction() {
        return action;
    }

    @Override
    public void show() {
        super.show();
    }
}
