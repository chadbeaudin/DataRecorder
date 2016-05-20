package com.datarecorder;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.codehaus.jackson.map.ObjectMapper;

public class DataRecorderMessage extends LoggingObject {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");

    // This order seems to dictate the order in which the values are written to the file. So leave them like this for
    // file readability
    private Date timeStamp;
    private long delayMillis;
    private HashMap<String, Object> properties;
    private Object body;

    /**
     * Default empty constructor
     */
    public DataRecorderMessage() {

    }

    public DataRecorderMessage(final Message message, final long timeStamp) {
        try {
            // this.setDelayMillis(delayMillis);
            this.timeStamp = new Date(timeStamp);

            // Get all the properties from the incoming message
            this.properties = getAllProperties(message);

            // Get the payload from the incoming message.
            if (message instanceof BytesMessage) {
                byte[] byteArray = null;
                final BytesMessage tmp = (BytesMessage) message;
                int len;
                len = (int) ((BytesMessage) message).getBodyLength();
                byteArray = new byte[len];
                tmp.readBytes(byteArray);
                this.body = byteArray;
            } else if (message instanceof TextMessage) {
                this.body = ((TextMessage) message).getText();
            }

        } catch (final JMSException e) {
            logger.error("Error reading from the incoming JMS message");
            logger.error("Stacktrace: ", e);
        }
    }

    private HashMap<String, Object> getAllProperties(final Message message) throws JMSException {
        final HashMap<String, Object> properties = new HashMap<String, Object>();
        final Enumeration srcProperties = message.getPropertyNames();
        while (srcProperties.hasMoreElements()) {
            final String propertyName = (String) srcProperties.nextElement();
            properties.put(propertyName, message.getObjectProperty(propertyName));
        }

        return properties;
    }

    /**
     * @return the properties
     */
    public HashMap<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param properties
     *            the properties to set
     */
    public void setProperties(final HashMap<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * @return the body
     */
    public Object getBody() {
        return body;
    }

    /**
     * @param body
     *            the body to set
     */
    public void setBody(final Object body) {
        this.body = body;
    }

    /**
     * @return the messageDelayMillis
     */
    public long getDelayMillis() {
        return delayMillis;
    }

    /**
     * @param delayMillis
     *            the messageDelayMillis to set
     */
    public void setDelayMillis(final long delayMillis) {
        this.delayMillis = delayMillis;
    }

    /**
     * Write this object out as a JSON string to a file with the provided path.
     * 
     * @param fileName
     */
    public void writeAsJsonToFile(final String fileName) {

        try {
            OBJECT_MAPPER.writeValue(new File(fileName), this);
        } catch (final IOException e) {
            logger.error("Error writing to file {}", fileName);
            logger.error("Stacktrace: ", e);
        }

    }

    public void getJmsMessage(final Message message) throws JMSException {

        for (final Map.Entry<String, Object> entry : properties.entrySet()) {
            final String propertyName = entry.getKey();
            final Object value = entry.getValue();
            message.setObjectProperty(propertyName, value);
        }

        if (message instanceof BytesMessage) {
            // ((BytesMessage)message).writeBytes(body);
        } else if (message instanceof TextMessage) {
            ((TextMessage) message).setText((String) body);
        }

    }

    @Override
    public String toString() {
        String json = null;
        try {
            json = OBJECT_MAPPER.writeValueAsString(this);
        } catch (final IOException e) {
            logger.error("Error converting to JSON.");
            logger.error("Stacktrace: ", e);
        }
        return json;

    }

    /**
     * @return the timeStamp
     */
    public String getTimeStamp() {
        return dateFormat.format(timeStamp);
    }

    /**
     * @param timeStamp
     *            the timeStamp to set
     */
    public void setTimeStamp(final String timeStamp) {
        try {
            this.timeStamp = dateFormat.parse(timeStamp);
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
