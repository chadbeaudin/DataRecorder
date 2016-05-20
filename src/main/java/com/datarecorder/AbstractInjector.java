/*
 * Created on Jan 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;

import com.datarecorder.util.ui.MessageBox;

/**
 * @author b1085685
 * 
 */
public abstract class AbstractInjector extends JPanel implements Injector {
    private static final long serialVersionUID = -229028255701102310L;
    private static final String PUBLISH = "Publish";
    private Publisher publisher;
    private JButton publishButton;

    private Logger logger = LoggingObject.getLogger(this.getClass());

    public AbstractInjector() {
        setLayout(new BorderLayout());
        final JPanel buttonPanel = new JPanel();
        publishButton = new JButton(PUBLISH);
        buttonPanel.add(publishButton);

        publishButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                publish();
            }
        });

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setPublisher(final Publisher publisher) {
        this.publisher = publisher;
    }

    protected void addContent(final JComponent panel) {
        add(panel, BorderLayout.CENTER);
    }

    /**
     * Method to obtain handle to display component
     * 
     * @return the JPanel
     */
    public JComponent getJComponent() {
        return this;
    }

    // mk?
    protected void showButtons(final boolean value) {
        publishButton.setVisible(value);
    }

    // mk?
    protected void showPublishButton(final boolean value) {
        publishButton.setVisible(value);
    }

    /**
     * Read the information object from the viewer for display.
     * 
     * @param bytes
     *            the data to be published
     */
    protected void publish(final byte[] bytes) {

        if (publisher == null) {
            MessageBox.showError(getParent(), "Publication was not initialized");
            return;
        }

        try {
            publisher.publish(bytes);
        } catch (final Exception ex) {
            logger.error("Error publishing.", ex);
            MessageBox.showError(getParent(), ex.getMessage());
        }
    }
}
