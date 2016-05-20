/*
 * Created on Jan 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder;

/**
 * @author b1085685
 *
 */
public class InvalidViewerMessageException extends Exception {

    private static final long serialVersionUID = 757056417041608685L;
    private String message;

    public InvalidViewerMessageException(final String value) {
        message = value;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
