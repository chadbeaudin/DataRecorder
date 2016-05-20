
package com.datarecorder.util.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/**
 * Parent class of JDialogs wanting to allow the Escape key to be pressed to close the dialog.
 * 
 * @author snam
 * @date Sep 21, 2007
 * @version 1.0
 */
public class EscapeDialog extends JDialog {

    private static final long serialVersionUID = 4360250608925654100L;

    public EscapeDialog(final Frame frame, final String title, final boolean modal) {
        super(frame, title, modal);
    }

    public EscapeDialog(final Dialog parent, final String title, final boolean modal) {
        super(parent, title, modal);
    }

    @Override
    protected JRootPane createRootPane() {

        final JRootPane rootPane = new JRootPane();

        final KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        final ActionListener escKeyListener = new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                doEscapePress();
            }
        };
        rootPane.registerKeyboardAction(escKeyListener, escStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Enter/Return key listener
        // the VK_ENTER key stroke does not seem to be capturable using this
        // method, although intercepting the VK_SPACE key stroke does work.
        // Specifically, JXTables do not seem to propagate the Enter key press
        // up to ancestors, so adding an VK_ENTER key listener here does not
        // work when the dialog has JXTables.

        return rootPane;
    }

    /**
     * set this dialog to not be visible. If subclasses need to intercept the close event they should override this
     * method and handle the closing event as they wish.
     */
    protected void doEscapePress() {
        setVisible(false);
    }
}
