package com.datarecorder.util.ui;



import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.slf4j.Logger;

import com.datarecorder.LoggingObject;

/**
 * Basic window utilities.
 * 
 * @author Mike Kline
 * @author Mark Temple
 */

public class WindowUtils {

    private static Logger logger = LoggingObject.getLogger(WindowUtils.class);

    public static void centerWindow(final Window win) {
        final Dimension wSize = win.getSize();
        final Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Point p = new Point((sSize.width - wSize.width) / 2, (sSize.height - wSize.height) / 2);
        if (p.x < 1)
            p.x = 1;
        if (p.y < 1)
            p.y = 1;
        win.setLocation(p);
    }

    public static void centerWindow(final Window win, final Component parent) {
        final Dimension wSize = win.getSize();
        final Rectangle pBounds = parent.getBounds();
        // This attempts to fixe a bug in AWT where centered windows
        // sometimes report (1,1) as their location.
        if ((pBounds.x == 1 && pBounds.y == 1) || (pBounds.width == 0 || pBounds.height == 0)) {
            centerWindow(win);
        } else {
            final Point p = new Point(pBounds.x + (pBounds.width - wSize.width) / 2,
                    pBounds.y + (pBounds.height - wSize.height) / 2);

            if (p.x < 1)
                p.x = 1;
            if (p.y < 1)
                p.y = 1;
            win.setLocation(p);
        }
    }

    public static Frame getParentFrame(final Component c) {
        Window w = getParentWindow(c);
        if (w instanceof Dialog) {
            if (w.getParent() instanceof Window) {
                w = (Window) w.getParent();
            }
        }

        if (w instanceof Frame) {
            return (Frame) w;
        }

        return null;
    }

    public static Window getParentWindow(final Component comp) {

        Component tmp = comp;
        Component parent = comp;
        while (true) {
            if (parent == null || parent instanceof Window) {
                break;
            }

            parent = tmp;

            if (tmp == null) {
                return null;
            }
            tmp = tmp.getParent();
        }

        return (Window) parent;
    }

    public static void setWaitCursor(final Component c, final boolean state) {
        c.setCursor(state ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    public static void setNativeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception e) {
            logger.warn("Error setting native look and feel", e);
        }
    }

    public static void setJavaLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (final Exception e) {
            logger.warn("Error setting Java look and feel", e);
        }
    }

    public static void setMotifLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (final Exception e) {
            logger.warn("Error setting Motif look and feel", e);
        }
    }

    public static JFrame openInJFrame(final Container content, final int width, final int height, final String title,
            final Color bgColor) {
        final JFrame frame = new JFrame(title);
        frame.setBackground(bgColor);
        content.setBackground(bgColor);
        frame.setSize(width, height);
        frame.setContentPane(content);
        addExitListener(frame);
        frame.setVisible(true);
        return (frame);
    }

    public static JFrame openInJFrame(final Container content, final int width, final int height, final String title) {
        return (openInJFrame(content, width, height, title, Color.white));
    }

    public static JFrame openInJFrame(final Container content, final int width, final int height) {
        return (openInJFrame(content, width, height, content.getClass().getName(), Color.white));
    }

    public static void addExitListener(final java.awt.Window parent) {
        parent.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                System.exit(0);
            }
        });
    }
}
