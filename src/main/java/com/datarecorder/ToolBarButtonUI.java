package com.datarecorder;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

import org.slf4j.Logger;

/**
 * Gives a button a unique L&F for a button bar
 * 
 * @version 1.1
 * @author Tim Cunningham
 */
public class ToolBarButtonUI extends BasicButtonUI {
    private static ToolBarButtonUI myButtonUI = new ToolBarButtonUI();
    private Border upBorder = ThinBorder.createBorder(ThinBorder.RAISED);
    private Border downBorder = ThinBorder.createBorder(ThinBorder.LOWERED);

    private Logger logger = LoggingObject.getLogger(this.getClass());

    /**
     * Return the UI
     * 
     * @param c
     *            The component
     * @return The UI
     */
    public static ComponentUI createUI(final JComponent c) {
        return myButtonUI;
    }

    /**
     * Installs the UI
     * 
     * @param c
     *            The component
     */
    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        c.setBorder(UIManager.getBorder("ToolBarButton.border"));
    }

    /**
     * Paints the Button
     * 
     * @param g
     *            The graphics
     * @param c
     *            The component
     */
    @Override
    public void paint(final Graphics g, final JComponent c) {
        // super.paint(g, c);
        final AbstractButton button = (AbstractButton) c;
        button.setRolloverEnabled(true);
        final ButtonModel model = button.getModel();
        final Rectangle bounds = button.getBounds();

        if (model.isPressed() && model.isArmed()) {
            g.translate(1, 1);
            super.paint(g, c);
            g.translate(-1, -1);
            downBorder.paintBorder(c, g, 0, 0, bounds.width, bounds.height);
        } else if (button.isRolloverEnabled() && model.isRollover()) {
            super.paint(g, c);
            upBorder.paintBorder(c, g, 0, 0, bounds.width, bounds.height);
        } else {
            super.paint(g, c);
        }
    }
}
