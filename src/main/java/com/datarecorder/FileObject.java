package com.datarecorder;

public class FileObject {

    private String fullname;

    public FileObject(final String fullname) {
        this.fullname = fullname;
    }

    public String getFullname() {
        return fullname;
    }

    @Override
    public String toString() {
        final int begin = fullname.lastIndexOf(System.getProperty("file.separator"));
        return fullname.substring(begin + 1);
    }
}
