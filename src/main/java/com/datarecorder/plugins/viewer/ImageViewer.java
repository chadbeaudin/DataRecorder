package com.datarecorder.plugins.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;

import com.datarecorder.DataRecorderMessage;
import com.datarecorder.InvalidViewerMessageException;
import com.datarecorder.LoggingObject;
import com.datarecorder.Viewer;



/**
 * The JavaImageViewer class interprets a serialized image icon object.
 */
public class ImageViewer extends JPanel implements Viewer {

    private static final long serialVersionUID = 5957657106528471836L;
    private static final String TYPE = "java image";
    private JLabel photographLabel = new JLabel();
    private JScrollPane scroller = new JScrollPane(photographLabel);

    private Logger logger = LoggingObject.getLogger(this.getClass());

    public ImageViewer() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("View as " + TYPE));
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setVerticalAlignment(JLabel.CENTER);
        photographLabel.setVerticalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);

        add(scroller, BorderLayout.CENTER);

    }

    @Override
    public void clear() {
        photographLabel.setIcon(null);
    }


    public void displayInformationObject(final byte[] object) throws InvalidViewerMessageException {
        final byte[] bytes = object;

        try {
            final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            final ImageIcon image = (ImageIcon) ois.readObject();
            photographLabel.setIcon(image);
        } catch (final IOException ex) {
            logger.error("Error reading image.", ex);
            throw new InvalidViewerMessageException("Message cannot be displayed as an image type!"
                    + "\nFreeze will be enabled and play is stopped on this channel..."
                    + "\nTo continue to play this channel, select \"ok\""
                    + "\nSelect an appropriate data type and disable freeze to continue viewing...");
        } catch (final ClassNotFoundException ex) {
            logger.error("Error reading message.", ex);
            throw new InvalidViewerMessageException("Message cannot be displayed as an image type!");
        }
    }

  
    @Override
    public void closed() {
        photographLabel.setIcon(null);
        photographLabel.setText("< subscription closed >");
    }

 
    @Override
    public JPanel getJPanel() {
        return this;

    }

    @Override
    public String toString() {
        return TYPE;
    }

    public static void main(final String[] args) {
        final JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(new ImageViewer());
        frame.setSize(new Dimension(400, 300));
        frame.dispose();

    }

    @Override
    public void displayInformationObject(final DataRecorderMessage infoObject) throws InvalidViewerMessageException {
        // TODO Auto-generated method stub

    }
}
