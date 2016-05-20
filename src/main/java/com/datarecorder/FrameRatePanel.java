/*
 * Created on Jan 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.datarecorder.util.ui.WindowUtils;

/**
 * @author b1085685
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class FrameRatePanel extends JPanel {

    private static final long serialVersionUID = -6605044332302710644L;
    JTextField minValue;
    JTextField maxValue;
    JSlider slider1;
    JSlider slider2;
    JLayeredPane layeredPane;
    JPanel panel1;
    JPanel panel2;

    boolean updating = false;

    public FrameRatePanel(final int min, final int max) {
        this();
        setMinimum(min);
        setMaximum(max);
    }

    public FrameRatePanel() {
        final int min = 0;
        final int max = 100;
        this.setLayout(new GridBagLayout());
        panel1 = new JPanel();
        panel2 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel2.setLayout(new GridBagLayout());

        layeredPane = new JLayeredPane();

        addComponentListener(new MyComponentListener());

        minValue = new JTextField(4);
        minValue.setHorizontalAlignment(JTextField.RIGHT);
        minValue.setEditable(false);
        maxValue = new JTextField(4);
        maxValue.setHorizontalAlignment(JTextField.RIGHT);
        maxValue.setEditable(false);

        slider1 = new JSlider();
        slider1.setBorder(BorderFactory.createTitledBorder("Begin Frame"));
        slider2 = new JSlider();
        slider2.setBorder(BorderFactory.createTitledBorder("End Frame"));

        slider1.setMinimum(min);
        slider1.setMaximum(max);
        slider1.setValue(min);

        slider2.setMinimum(min);
        slider2.setMaximum(max);
        slider2.setValue(max);

        slider1.setPaintTicks(true);
        slider1.setMajorTickSpacing(max);
        slider2.setPaintTicks(true);
        slider2.setMajorTickSpacing(max);

        slider1.addChangeListener(new ChangeListener() {
            public void stateChanged(final ChangeEvent e) {
                if (slider1.getValue() > slider2.getValue())
                    slider2.setValue(slider1.getValue());

                setValues();
            }
        });
        slider2.addChangeListener(new ChangeListener() {
            public void stateChanged(final ChangeEvent e) {

                if (slider2.getValue() < slider1.getValue())
                    slider1.setValue(slider2.getValue());

                setValues();

            }
        });
        panel1.add(slider1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
        panel1.add(slider2, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
        panel2.add(minValue, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
                GridBagConstraints.NONE, new Insets(50, 4, 4, 4), 0, 0));

        panel2.add(maxValue, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
                GridBagConstraints.NONE, new Insets(50, 4, 4, 4), 0, 0));

        panel1.setOpaque(false);
        panel2.setOpaque(false);
        layeredPane.add(panel1, Integer.valueOf(0), 0);
        layeredPane.add(panel2, Integer.valueOf(1), 1);

        this.add(layeredPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));

        setValues();

    }

    /**
     * Set the min range of the slider
     * 
     * @param value
     */
    public void setMinimum(final int value) {
        slider1.setMinimum(value);
        slider2.setMinimum(value);
        slider1.setValue(value);
    }

    /**
     * Set the max range of the slider
     * 
     * @param value
     */
    public void setMaximum(final int value) {
        slider1.setMaximum(value);
        slider2.setMaximum(value);
        slider2.setValue(value);
    }

    /**
     * Get the currently selected minvalue
     * 
     * @return Integer
     */
    public Integer getMinimumValue() {
        // if problems then get the value from the slider
        return new Integer(minValue.getText());
    }

    /**
     * Get the currently selected max value
     * 
     * @return Integer
     */
    public Integer getMaximumValue() {
        // if problems then get the value from the slider
        return new Integer(maxValue.getText());
    }

    @Override
    public void setVisible(final boolean value) {
        if (value == true) {
            minValue.doLayout();
            maxValue.doLayout();
        }
    }

    protected void setValues() {
        minValue.setText(Integer.toString(slider1.getValue()));
        maxValue.setText(Integer.toString(slider2.getValue()));
    }

    private class MyComponentListener implements ComponentListener {
        public void componentHidden(final ComponentEvent c) {
        }

        public void componentShown(final ComponentEvent c) {
        }

        public void componentMoved(final ComponentEvent c) {
        }

        public void componentResized(final ComponentEvent c) {
            final Component source = (Component) c.getSource();
            final int width = source.getWidth();
            final int height = source.getHeight();
            panel1.setSize(width - 5, height);
            panel2.setSize(width - 5, height);
            panel1.doLayout();
            panel2.doLayout();
        }
    }

    /*
     * private class MinimumListener implements ChangeListener { private JSpinner relatedSpinner; public
     * MinimumListener(JSpinner relatedSpinner) { this.relatedSpinner = relatedSpinner; } public void
     * stateChanged(ChangeEvent e) { if(!updating) { updating = true; System.out.println("min"); JSpinner spinner =
     * (JSpinner)e.getSource(); SpinnerNumberModel dm = (SpinnerNumberModel)relatedSpinner.getModel();
     * SpinnerNumberModel sm = (SpinnerNumberModel)spinner.getModel(); Comparable comparable =
     * (Comparable)sm.getValue(); Comparable comparable1 = (Comparable)dm.getValue(); dm.setMinimum(comparable);
     * if(comparable1.compareTo(comparable) < 0) dm.setValue(comparable); setSliderValues(); updating = false; } } }
     * private class MaximumListener implements ChangeListener { private JSpinner relatedSpinner; public
     * MaximumListener(JSpinner relatedSpinner) { this.relatedSpinner = relatedSpinner; } public void
     * stateChanged(ChangeEvent e) { if(!updating) { System.out.println("max"); updating = true; JSpinner spinner =
     * (JSpinner)e.getSource(); SpinnerNumberModel dm = (SpinnerNumberModel)relatedSpinner.getModel();
     * SpinnerNumberModel sm = (SpinnerNumberModel)spinner.getModel(); Comparable comparable =
     * (Comparable)sm.getValue(); Comparable comparable1 = (Comparable)dm.getValue(); dm.setMaximum(comparable);
     * if(comparable1.compareTo(comparable) > 0) dm.setValue(comparable); setSliderValues(); updating = false; } } }
     */
    public static void main(final String[] args) {
        final JFrame fr = new JFrame("Test Window");
        WindowUtils.setNativeLookAndFeel();
        fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        final FrameRatePanel panel = new FrameRatePanel(0, 0);
        fr.getContentPane().add(panel);
        fr.setSize(300, 150);
        WindowUtils.centerWindow(fr);
        fr.setVisible(true);
        panel.setMaximum(1000);
        panel.setMinimum(20);
    }
}
