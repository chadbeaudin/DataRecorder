/*
 * Created on Jan 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder.util.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;

import com.datarecorder.DataRecorder;

/**
 * @author b1085685
 *
 */
public class DialogTitlePane extends JPanel {

    private static final long serialVersionUID = -2922928030259466683L;

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 12);

    private JLabel iconLabel;
    private JLabel titleLabel;

    public DialogTitlePane(final String title, final String details, final ImageIcon icon) {
        setLayout(new GridBagLayout());
        setBackground(Color.white);

        iconLabel = new JLabel(icon);
        createTitleLabel(title);

        add(titleLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(12, 12, 0, 0), 0, 0));
        add(iconLabel, new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0, GridBagConstraints.NORTHEAST,
                GridBagConstraints.NONE, new Insets(12, 0, 0, 12), 0, 0));
        add(new JLabel(), new GridBagConstraints(0, 2, 2, 0, 1.0, 1.0, GridBagConstraints.NORTHEAST,
                GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    }

    public void setTitle(final String value) {
        titleLabel.setText(value);
    }

    public void setDetails(final String value) {
        titleLabel.setToolTipText(value);
    }

    /**
     * @param title
     */
    private void createTitleLabel(final String title) {
        titleLabel = new JLabel(title) {
            private static final long serialVersionUID = 5718187133336812562L;

            @Override
            public JToolTip createToolTip() {
                return new MultiLineToolTip();
            }
        };
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setBackground(Color.white);
        titleLabel.setOpaque(true);
    }

    public static void main(final String[] args) {
        final JFrame fr = new JFrame("test");
        // Details
        final URL url = DataRecorder.class.getResource("/com/tools/common/resources/Details24.gif");
        final ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(url));

        fr.getContentPane().add(new DialogTitlePane("Test", "This is a test of the emergency broadcast center", icon));
        fr.setSize(new Dimension(300, 400));
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setVisible(false);

    }
}
