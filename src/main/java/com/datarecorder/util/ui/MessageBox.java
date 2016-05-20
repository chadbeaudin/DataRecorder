package com.datarecorder.util.ui;



/**
 * Display message dialogs.
 * 
 * @author Mike Kline
 * @author Mark Temple
 */
public class MessageBox {
    private static javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();

    public static void showError(final String value, final java.awt.Component parent) {
        final javax.swing.JOptionPane pane = new javax.swing.JOptionPane(value, javax.swing.JOptionPane.ERROR_MESSAGE);
        final javax.swing.JDialog dialog = pane.createDialog(parent, "Error");
        dialog.setVisible(true);
    }

    public static void showInfo(final String value, final java.awt.Component parent) {
        final javax.swing.JOptionPane pane = new javax.swing.JOptionPane(value,
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        final javax.swing.JDialog dialog = pane.createDialog(parent, "Message");
        dialog.setVisible(true);
    }

    public static void showWarning(final String value, final java.awt.Component parent) {
        final javax.swing.JOptionPane pane = new javax.swing.JOptionPane(value,
                javax.swing.JOptionPane.WARNING_MESSAGE);
        final javax.swing.JDialog dialog = pane.createDialog(parent, "Warning");
        dialog.setVisible(true);
    }

    public static int showConfirm(final String value, final java.awt.Component parent) {
        return javax.swing.JOptionPane.showConfirmDialog(null, value, "Confirm", javax.swing.JOptionPane.YES_NO_OPTION);
    }

    public static java.awt.Color showColor(final java.awt.Color current, final java.awt.Component parent) {
        return javax.swing.JColorChooser.showDialog(parent, "Choose Background Color", current);
    }

    public static java.io.File showFileOpen(final java.awt.Component parent) {
        java.io.File file = null;
        final int returnVal = fileChooser.showOpenDialog(parent);
        if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        return file;
    }

    public static java.io.File showFileSave(final java.awt.Component parent) {
        java.io.File file = null;
        final int returnVal = fileChooser.showSaveDialog(parent);
        if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        return file;
    }

    // from td 2.3
    public static void showError(final java.awt.Component parent, final String value) {
        final javax.swing.JOptionPane pane = new javax.swing.JOptionPane(value, javax.swing.JOptionPane.ERROR_MESSAGE);
        final javax.swing.JDialog dialog = pane.createDialog(parent, "Error");
        dialog.setVisible(true);
    }

    public static void showInfo(final java.awt.Component parent, final String value) {
        final javax.swing.JOptionPane pane = new javax.swing.JOptionPane(value,
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        final javax.swing.JDialog dialog = pane.createDialog(parent, "Message");
        dialog.setVisible(true);
    }

    public static void showWarning(final java.awt.Component parent, final String value) {
        final javax.swing.JOptionPane pane = new javax.swing.JOptionPane(value,
                javax.swing.JOptionPane.WARNING_MESSAGE);
        final javax.swing.JDialog dialog = pane.createDialog(parent, "Warning");
        dialog.setVisible(true);
    }

    public static int showConfirm(final java.awt.Component parent, final String value) {
        return javax.swing.JOptionPane.showConfirmDialog(null, value, "Confirm", javax.swing.JOptionPane.YES_NO_OPTION);
    }

    public static java.awt.Color showColor(final java.awt.Component parent, final java.awt.Color current) {
        return javax.swing.JColorChooser.showDialog(parent, "Choose Background Color", current);
    }

}
