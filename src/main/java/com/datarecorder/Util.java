package com.datarecorder;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.slf4j.Logger;

import com.datarecorder.util.ui.MessageBox;

public class Util {
    private static final String MENULABEL = ".MenuLabel";
    private static final String SMALLICON = ".SmallIcon";
    private static final String TOOLTIP = ".Tooltip";
    private static final String LABEL = ".Label";
    private static final String ICON = ".Icon";
    private static HashMap<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>();
    private static FileInputStream fis;
    private static FileOutputStream fos;

    private static Logger logger = LoggingObject.getLogger(Util.class);

    public static Properties getPropertyFile(final Component parent, final String filename) {
        final Properties propertyFile = new Properties();
        try {
            fis = new FileInputStream(filename);
            propertyFile.load(fis);
            return propertyFile;
        } catch (final FileNotFoundException e1) {
            // file not found, will generate on save
            propertyFile.setProperty(ChannelViewer.FILTER + ".text",
                    "com.datarecorder.plugins.viewer.TextViewer");
            propertyFile.setProperty(ChannelViewer.FILTER + ".html",
                    "com.datarecorder.plugins.viewer.HtmlViewer");
            propertyFile.setProperty(ChannelViewer.FILTER + ".image",
                    "com.datarecorder.plugins.viewer.ImageViewer");
            propertyFile.setProperty(ChannelInjector.FILTER + ".text",
                    "com.datarecorder.plugins.injector.TextInjector");
            propertyFile.setProperty(ChannelInjector.FILTER + ".image",
                    "com.datarecorder.plugins.injector.ImageInjector");
            propertyFile.setProperty(DataRecorder.FILTER + ".iconOnly", "true");
        } catch (final IOException e1) {
            MessageBox.showWarning(parent, "Error reading:" + filename);
            logger.warn("Error reading: {}", filename, e1);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (final Exception e) {
                logger.error("Error closing file.", e);
            }
        }

        return propertyFile;
    }

    public static void saveProperties(final Component parent, final Properties properties, final String filename) {
        try {
            fos = new FileOutputStream(filename);
            properties.store(fos, "Data Recorder Properties");
        } catch (final FileNotFoundException e) {
            MessageBox.showWarning(parent, "File not found:" + filename);
            logger.error("File not found: {}", filename, e);
        } catch (final IOException e) {
            MessageBox.showWarning(parent, "Error writing:" + filename);
            logger.error("Error writing: ", filename, e);
        } finally {
            try {
                fos.close();
            } catch (final IOException e) {
                logger.error("Error closing file.", e);
            }
        }
    }

    public static ImageIcon getImageFromResource(final ResourceBundle bundle, final String name) {
        final String imagePath = bundle.getString(name);
        return getImage(imagePath);
    }

    public static ImageIcon getImage(final String path) {
        ImageIcon icon = null;

        if (iconCache.containsKey(path))
            icon = iconCache.get(path);
        else {
            final URL url = DataRecorder.class.getResource(path);
            if (url != null) {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(url));
                iconCache.put(path, icon);
            }
        }
        return icon;
    }

    public static JButton createButton(final String resourceKey) {
        JButton button = null;
        final String iconName = DataRecorder.bundle.getString(resourceKey + ICON);
        final URL url = DataRecorder.class.getResource(iconName);
        if (url != null)
            button = new JButton(DataRecorder.bundle.getString(resourceKey + LABEL),
                    new ImageIcon(Toolkit.getDefaultToolkit().getImage(url)));
        else
            button = new JButton(DataRecorder.bundle.getString(resourceKey + LABEL), new ImageIcon(iconName));
        // button.setUI(new ToolBarButtonUI());
        button.setToolTipText(DataRecorder.bundle.getString(resourceKey + TOOLTIP));
        button.setRolloverEnabled(true);
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setFocusable(false);
        return button;
    }

    /**
     * Used for standard menu items
     * 
     * @param resourceKey
     * @param key
     * @param mask
     * @return the JMenuItem for the specified keys
     */
    public static JMenuItem createMenuItem(final String resourceKey, final int key, final int mask) {
        JMenuItem menuItem;
        final String iconName = DataRecorder.bundle.getString(resourceKey + SMALLICON);
        final URL url = DataRecorder.class.getResource(iconName);
        if (url != null) {
            menuItem = new JMenuItem(DataRecorder.bundle.getString(resourceKey + MENULABEL),
                    new ImageIcon(Toolkit.getDefaultToolkit().getImage(url)));
        } else {
            menuItem = new JMenuItem(DataRecorder.bundle.getString(resourceKey + MENULABEL), new ImageIcon(iconName));
        }
        menuItem.setAccelerator(KeyStroke.getKeyStroke(key, mask));
        return menuItem;
    }

    /**
     * Used for pop-ups with no accelerators.
     * 
     * @param resourceKey
     * @return the JMenuItem for the resourceKey
     */
    public static JMenuItem createPopUpMenuItem(final String resourceKey) {
        return createMenuItem(resourceKey, 0, 0);
    }

    public static JMenu createMenu(final String name, final int mnemonic) {
        final JMenu menu = new JMenu(name);
        menu.setMnemonic(mnemonic);
        return menu;
    }
}
