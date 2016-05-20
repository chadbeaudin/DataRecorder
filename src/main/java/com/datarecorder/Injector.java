/*
 * Created on Jan 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder;

import javax.swing.JComponent;

/**
 * @author b1085685
 *
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public interface Injector {

    /**
     * Method to obtain handle to display component
     * 
     * @return the JPanel
     */
    public JComponent getJComponent();

    /**
     * Invoke a clear action from the SubscriptionViewer
     */
    public void clear();

    /**
     * Invoke publish action
     *
     */
    public void publish();

    /**
     * Invoke publish action
     *
     */
    public void setPublisher(Publisher publication);

}
