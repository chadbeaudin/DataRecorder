/*
 * Created on Jan 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder;

/**
 * @author b1085685
 *
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public interface Publisher {
    /**
     * Called to publish data to the server.
     * 
     * @param bytes
     *            the message
     * @throws Exception,
     *             if unable to publish the data.
     */
    public void publish(byte[] bytes) throws Exception;

    /**
     * Get the name of the type of the publication. Used by the Channel Injector to determine if a default injector
     * exists.
     * 
     * @return the name of the information type of the channel
     */
    public String getPublicationTypeName();
}
