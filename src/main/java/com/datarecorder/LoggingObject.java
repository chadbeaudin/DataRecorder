package com.datarecorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When possible extend this class to get the logger. If you are unable to extend this, you should use the static call
 * to getLogger();
 */
public class LoggingObject {
    /**
     * Logger
     */
    protected final Logger logger = getLogger(this.getClass());

    /**
     * Static call to get a logger if you can't extend {@link LoggingObject}
     * 
     * @param userClass
     * @return logger
     */
    public static Logger getLogger(final Class<?> userClass) {
        return LoggerFactory.getLogger(userClass.getName());
    }

}
