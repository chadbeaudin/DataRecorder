package com.datarecorder.plugins.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import com.datarecorder.DataRecorderMessage;
import com.datarecorder.InvalidViewerMessageException;
import com.datarecorder.Viewer;
import com.datarecorder.util.ui.MessageBox;

/**
 * @author b1085685
 *
 */
public class HtmlViewer extends JPanel implements Viewer {

    private static final long serialVersionUID = -2903445353142560926L;
    private static final String TEXT_HTML = "text/html";
    private static final String TEXT_PLAIN = "text/plain";

    private static final String TYPE = "html";
    private JEditorPane editor = new JEditorPane();
    private JScrollPane scroller = new JScrollPane(editor);

    public HtmlViewer() {
        setLayout(new BorderLayout());

        editor.setContentType(TEXT_HTML);
        add(scroller, BorderLayout.CENTER);
        editor.setEditable(false);
        editor.addHyperlinkListener(new Hyperactive());
        this.setBackground(Color.RED);
    }

    @Override
    public void clear() {
        editor.setText("");
    }


    public void displayInformationObject(final byte[] object) {
        final String value = new String(object);
        if (value.indexOf("html") >= 0 || value.indexOf("HTML") >= 0)
            editor.setContentType(TEXT_HTML);
        else
            editor.setContentType(TEXT_PLAIN);
        editor.setText(new String(object));
    }

 
    @Override
    public void closed() {
        editor.setText("< subscription closed >");
    }

    @Override
    public String toString() {
        return TYPE;
    }

    public static void main(final String[] args) {
        final JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(new HtmlViewer());
        frame.setSize(new Dimension(400, 300));
        frame.dispose();

    }

  
    @Override
    public JPanel getJPanel() {
        return this;

    }

    class Hyperactive implements HyperlinkListener {

        @Override
        public void hyperlinkUpdate(final HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                final JEditorPane pane = (JEditorPane) e.getSource();
                if (e instanceof HTMLFrameHyperlinkEvent) {
                    final HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                    final HTMLDocument doc = (HTMLDocument) pane.getDocument();
                    doc.processHTMLFrameHyperlinkEvent(evt);
                } else {
                    try {
                        pane.setPage(e.getURL());
                    } catch (final Throwable t) {
                        MessageBox.showError(HtmlViewer.this.getParent(), t.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void displayInformationObject(final DataRecorderMessage infoObject) throws InvalidViewerMessageException {
        // TODO Auto-generated method stub

    }
}
