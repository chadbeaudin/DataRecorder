/*
 * Created on Jan 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder.plugins.injector;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.datarecorder.AbstractInjector;

/**
 * @author b1085685
 *
 */
public class TextInjector extends AbstractInjector {

    private static final long serialVersionUID = -7371764228771040638L;
    private static final String EMPTY_STRING = "";
    JTextArea textfield = new JTextArea();
    JScrollPane scroller = new JScrollPane(textfield);

    public TextInjector() {
        super.addContent(scroller);
    }

  
    public void clear() {
        textfield.setText(EMPTY_STRING);

    }


    public void publish() {
        super.publish(textfield.getText().getBytes());
    }
}
