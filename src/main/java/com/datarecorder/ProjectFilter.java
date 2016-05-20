package com.datarecorder;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ProjectFilter extends FileFilter {
    @Override
    public boolean accept(final File f) {
        if (f.isDirectory()) {
            return true;
        }

        if (getExtension(f))
            return true;

        return false;
    }

    private boolean getExtension(final File f) {
        final String file = f.getName();
        return file.endsWith(".drp");
    }

    @Override
    public String getDescription() {
        return "Data Recorder Project (*.drp)";
    }
}
