/**
 * Created on Jan 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder;

import javax.swing.JPanel;


/**
 * @author b1085685
 *
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public interface Viewer {
    /**
     * Method to obtain handle to display component
     * 
     * @return the JPanel
     */
    public JPanel getJPanel();

    /**
     * Invoke a clear action from the SubscriptionViewer
     */
    public void clear();

    /**
     * Pass the information object to the viewer for display.
     * 
     * @param infoObject
     * @throws InvalidViewerMessageException
     */
    public void displayInformationObject(DataRecorderMessage infoObject) throws InvalidViewerMessageException;

    /**
     * Let's the viewer know that the subscription has been closed.
     */
    public void closed();
}
