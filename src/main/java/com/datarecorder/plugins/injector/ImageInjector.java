/*
 * Created on Jan 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder.plugins.injector;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.datarecorder.AbstractInjector;
import com.datarecorder.util.ui.MessageBox;
import com.datarecorder.util.ui.WindowUtils;

/**
 * @author b1085685
 *
 */
public class ImageInjector extends AbstractInjector {

    private static final long serialVersionUID = -741845687895193744L;
    private static final String EMPTY_STRING = "";
    private JLabel photographLabel = new JLabel();
    private JScrollPane scroller = new JScrollPane(photographLabel);
    private JTextField textfield = new JTextField();
    public static final JFileChooser fileChooser = new JFileChooser();

    public ImageInjector() {
        final JPanel contents = new JPanel();
        contents.setLayout(new BorderLayout());

        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setVerticalAlignment(JLabel.CENTER);
        photographLabel.setVerticalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);

        contents.add(scroller, BorderLayout.CENTER);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Choose Image file:"));
        final JButton lookupButton = new JButton("Select Image");
        panel.add(lookupButton, BorderLayout.CENTER);
        lookupButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {

                WindowUtils.setWaitCursor(ImageInjector.this, true);
                final int returnVal = fileChooser.showOpenDialog(getParent());
                WindowUtils.setWaitCursor(ImageInjector.this, false);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // format message
                    final Image img = Toolkit.getDefaultToolkit()
                            .getImage(fileChooser.getSelectedFile().getAbsolutePath());

                    final ImageIcon image = new ImageIcon(img);
                    photographLabel.setIcon(image);
                }
            }
        });
        contents.add(panel, BorderLayout.SOUTH);
        super.addContent(contents);
    }

 
    public void clear() {
        textfield.setText(EMPTY_STRING);
        photographLabel.setIcon(null);
    }

 
    public void publish() {
        try {
            if (photographLabel.getIcon() != null) {
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                final ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(photographLabel.getIcon());

                super.publish(bos.toByteArray());
            } else {
                MessageBox.showWarning(getParent(), "No image selected to publish");
            }
        } catch (final IOException ex) {
            MessageBox.showError(getParent(), ex.getMessage());
        }
    }
}
