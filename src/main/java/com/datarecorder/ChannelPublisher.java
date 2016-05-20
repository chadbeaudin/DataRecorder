/*
 * Created on Jan 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.datarecorder;

import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;

import com.datarecorder.util.MessagingUtils;

/**
 * @author b1085685
 * 
 */
public class ChannelPublisher implements Publisher {
    private Vector<Object> v;
    private DataRecorder recorder;
    private MessagingUtils messagingUtils;
    private String channel;
    private Session topicSession;
    private MessageProducer topicPublisher;
    private ChannelViewer channelViewer;
    private TextMessage textMessage;
    private Boolean isQueue = false;

    private Logger logger = LoggingObject.getLogger(this.getClass());

    public ChannelPublisher(final DataRecorder recorder, final Vector<Object> producerVector) {
        this.recorder = recorder;
        this.v = producerVector;
        channel = (String) producerVector.get(PublisherTableValues.CHANNEL);
        messagingUtils = (MessagingUtils) producerVector.get(PublisherTableValues.BROKER);

        if (((String) producerVector.get(PublisherTableValues.TYPE)).equals("Q")) {
            isQueue = true;
        }
        logger.debug("Producer on [{}] is a queue: {}", channel, isQueue);

        topicPublisher = messagingUtils.createProducer(channel, isQueue, false);
        topicSession = messagingUtils.getSession();
        channelViewer = (ChannelViewer) producerVector.get(PublisherTableValues.CHANNEL_VIEWER);
        try {
            textMessage = topicSession.createTextMessage();
        } catch (final JMSException e) {
            logger.error("Error creating TextMessage", e);
        }
    }

    /**
     * @see com.datarecorder.Publisher#publish(byte[])
     */
    @Override
    public synchronized void publish(final byte[] bytes) throws Exception {
        textMessage.setText(new String(bytes));
        topicPublisher.send(textMessage);
        // count
        int i = Integer.parseInt((String) v.get(PublisherTableValues.COUNT));
        i++;
        v.set(PublisherTableValues.COUNT, Integer.toString(i));
        // display
        if (channelViewer != null && channelViewer.isShowing())
            // TODO update this to property display the json message??
            // channelViewer.displayInformationObject(bytes);
            // repaint
            this.recorder.publisherTable.repaint();

    }

    public synchronized void publish(final DataRecorderMessage dataRecorderMessage) throws JMSException {
        dataRecorderMessage.getJmsMessage(textMessage);
        topicPublisher.send(textMessage);
        // count
        int i = Integer.parseInt((String) v.get(PublisherTableValues.COUNT));
        i++;
        v.set(PublisherTableValues.COUNT, Integer.toString(i));
        // display
        if (channelViewer != null && channelViewer.isShowing())
            channelViewer.displayInformationObject(dataRecorderMessage);
        // repaint
        this.recorder.publisherTable.repaint();

    }

    /**
     * @see com.datarecorder.Publisher#getPublicationTypeName()
     */
    @Override
    public String getPublicationTypeName() {
        return "";
    }
}
