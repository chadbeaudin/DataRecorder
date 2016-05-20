/*
 * Created on Dec 3, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.datarecorder.util.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author mkline
 *
 *         To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code
 *         Generation&gt;Code and Comments
 */
public class AboutDialog extends EscapeDialog {

    private static final long serialVersionUID = -166519211725933912L;
    JLabel _text;

    /**
     * @param owner
     * @param title
     * @param text
     * @param modal
     */
    public AboutDialog(final Frame owner, final String title, final String text, final boolean modal) {
        super(owner, title, modal);
        init(text);
        pack();
    }

    public AboutDialog(final Frame owner, final String title, final boolean modal) {
        super(owner, title, modal);
        init(null);
        pack();
    }

    private void init(final String text) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(final WindowEvent e) {
                _text.requestFocus();
            }

            @Override
            public void windowActivated(final WindowEvent e) {
                _text.requestFocus();
            }
        });

        _text = new JLabel(text);
        _text.setBackground(getBackground());
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(""));
        panel.add(_text);
        getContentPane().setLayout(new BorderLayout());

        final JPanel buttonPanel = new JPanel();
        final JButton okButton = new JButton("OK");
        getRootPane().setDefaultButton(okButton);
        buttonPanel.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                dispose();
            }
        });
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Set the text of the box.
     * 
     * @param text
     *            an html string for multi-line messages
     */
    public void setText(final String text) {
        _text.setText(text);
    }

    /**
     * Set the text of the box.
     * 
     * @param title
     *            an html string for multi-line messages
     */
    @Override
    public void setTitle(final String title) {
        super.setTitle(title);
    }

    @Override
    public void show() {
        WindowUtils.centerWindow(this, this.getOwner());
        super.show();
    }

    public static void main(final String[] args) {
        new AboutDialog(null, "About Test",
                "<html><b>Program Name</b><br>Version XX<br>The Description of the project/tool.<br>",
                true).show();
    }
}
