package com.datarecorder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

import org.slf4j.Logger;

/**
 * User defined Borders
 * 
 * @version 1.1
 * @author Tim Cunningham
 */
public class ThinBorder extends AbstractBorder {

    private static final long serialVersionUID = 1L;
    public static final int RAISED = 0;
    public static final int LOWERED = 1;
    protected int thinType;
    protected Color highlight;
    protected Color shadow;
    private static ThinBorder raisedBorder = new ThinBorder(RAISED);
    private static ThinBorder loweredBorder = new ThinBorder(LOWERED);

    private Logger logger = LoggingObject.getLogger(this.getClass());

    public static ThinBorder createBorder(final int type) {
        if (type == RAISED)
            return raisedBorder;
        else
            return loweredBorder;
    }

    public static ThinBorder createBorder(final int type, final Color shadow, final Color highlight) {
        return new ThinBorder(type, shadow, highlight);
    }

    /**
     * Paints the border for the specified component with the specified position and size.
     *
     * @param c
     *            the component for which this border is being painted
     * @param g
     *            the paint graphics
     * @param x
     *            the x position of the painted border
     * @param y
     *            the y position of the painted border
     * @param width
     *            the width of the painted border
     * @param height
     *            the height of the painted border
     */
    @Override
    public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width,
            final int height) {
        final int w = width;
        final int h = height;
        g.translate(x, y);

        final Color currentShadow = (shadow == null) ? UIManager.getColor("controlShadow") : shadow;
        final Color currentHighlight = (highlight == null) ? UIManager.getColor("controlLtHighlight") : highlight;
        g.setColor(thinType == RAISED ? currentShadow : currentHighlight);
        g.drawRect(0, 0, w - 1, h - 1);

        g.setColor(thinType == RAISED ? currentHighlight : currentShadow);
        g.drawLine(0, 0, w - 2, 0);
        g.drawLine(0, 1, 0, h - 2);

        g.translate(-x, -y);
    }

    /**
     * Returns the insets of the border.
     * 
     * @param c
     *            the component for which this border insets value applies
     */
    @Override
    public Insets getBorderInsets(final Component c) {
        return new Insets(1, 1, 1, 1);
    }

    /**
     * Reinitialize the insets parameter with this Border's current Insets.
     * 
     * @param c
     *            the component for which this border insets value applies
     * @param insets
     *            the object to be reinitialized
     */
    @Override
    public Insets getBorderInsets(final Component c, final Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = 1;
        return insets;
    }

    /**
     * Returns whether or not the border is opaque.
     */
    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    /**
     * Returns which thin-type is set on the thin border.
     */
    public int getThinType() {
        return thinType;
    }

    /**
     * Creates a thin border with the specified thin-type whose whose colors will be derived from the background color
     * of the component passed into the paintBorder method.
     * 
     * @param thinType
     *            the type of the border
     */
    private ThinBorder(final int thinType) {
        this(thinType, null, null);
    }

    /**
     * Creates a thin border with the specified thin-type, shadow and highlight
     * 
     * @param thinType
     *            the type of the border
     * @param shadow
     *            the color of the shadow
     * @param highlight
     *            the color of the highlight
     */
    private ThinBorder(final int thinType, final Color shadow, final Color highlight) {
        this.thinType = thinType;
        this.shadow = shadow;
        this.highlight = highlight;
    }
}
