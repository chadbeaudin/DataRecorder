package com.datarecorder.plugins.viewer;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import com.datarecorder.DataRecorderMessage;
import com.datarecorder.Viewer;

/**
 * @author b1085685
 *
 */
public class TextViewer extends JPanel implements Viewer {

    private static final long serialVersionUID = 6533112916531229555L;
    private static final String EMPTY_STRING = "";
    private static final String SUBSCRIPTION_CLOSED = "< subscription closed >";
    private static final String NEW_LINE = "\n";
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final String TYPE = "text";
    private JTextArea textfield = new JTextArea();
    private JScrollPane scroller = new JScrollPane(textfield);
    private JCheckBox showLast = new JCheckBox("Show last message only");
    private JCheckBox showMessageOnly = new JCheckBox("Show message body only");
    private JLabel bufferLabel = new JLabel("Buffer Size:");
    private JSpinner bufferSpinner;
    private SpinnerNumberModel bufferModel;
    private MyLinkedList list = new MyLinkedList();

    public TextViewer() {
        setLayout(new GridBagLayout());
        bufferModel = new SpinnerNumberModel(500, 2, 10000, 1);
        bufferSpinner = new JSpinner(bufferModel);

        add(scroller, new GridBagConstraints(0, 0, 4, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(4, 4, 4, 4), 0, 0));

        add(showLast, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(4, 0, 4, 4), 0, 0));
        add(showMessageOnly, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(4, 0, 4, 4), 0, 0));
        add(new JLabel(EMPTY_STRING), new GridBagConstraints(1, 1, 1, 1, 2.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
        add(bufferLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        add(bufferSpinner, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
        
        textfield.setFont(new Font("Helvetica", Font.PLAIN, 12));
        
        showLast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                showLastSelected();
            }
        });
    }

    @Override
    public void clear() {
        textfield.setText(EMPTY_STRING);
        list.clear();
    }

    private void showLastSelected() {
        if (showLast.isSelected()) {
            bufferLabel.setEnabled(false);
            bufferSpinner.setEnabled(false);
        } else {
            bufferLabel.setEnabled(true);
            bufferSpinner.setEnabled(true);
        }
    }

    @Override
    public void displayInformationObject(final DataRecorderMessage object) {
        String displayText;
        if (showMessageOnly.isSelected()) {
            displayText = object.getBody().toString();
        } else {
            displayText = object.toString();
        }
        if (showLast.isSelected())
            textfield.setText(displayText);
        // textfield.setText(new String(object.getBytes()));
        else {
            // list.addLast(bufferModel.getNumber().intValue(), new String(object.getBytes()));
            list.addLast(bufferModel.getNumber().intValue(), displayText);
            textfield.setText(list.toString());
            scroller.getVerticalScrollBar().setValue(scroller.getVerticalScrollBar().getMaximum());
        }

    }

  
    @Override
    public void closed() {
        textfield.append(SUBSCRIPTION_CLOSED);
        textfield.append(NEW_LINE);

    }

    @Override
    public String toString() {
        return TYPE;
    }

    @Override
    public JPanel getJPanel() {
        return this;

    }

    private static class MyLinkedList extends LinkedList<Object> {

        private static final long serialVersionUID = -5647318197960528992L;
        private int numElements = 0;

        @Override
        public String toString() {
            final StringBuffer buf = new StringBuffer();
            final ListIterator<Object> iter = listIterator();
            while (iter.hasNext()) {
                buf.append(iter.next());
                buf.append(NEW_LINE);
            }
            return buf.toString();
        }

        public void addLast(final int size, final Object value) {
            numElements = size;
            while (size() >= numElements) {
                removeFirst();
            }
            super.addLast(value);
        }
    }

    public static void main(final String[] args) {
        final JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(new TextViewer());
        frame.setSize(new Dimension(400, 300));
        frame.dispose();

    }
}
