package com.datarecorder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;

import com.datarecorder.util.MessagingUtils;

class SubscriberThread extends Thread implements MessageListener {
    private Logger logger = LoggingObject.getLogger(this.getClass());

    private final DataRecorder recorder;
    private final Vector<Object> subscriberVector;
    private String channel;
    private Session session;
    private ChannelViewer channelViewer;
    private FileObject file;
    private boolean isPaused = false;
    private boolean isFirstTime = true;

    private boolean insertStartTimestamp;
    private long startTimeMillis;

    private DataOutputStream dos;
    private long currentTime;
    private long lastTime;
    private MessagingUtils messagingUtils;

    private long subscriberDelayMillis;
    private boolean delaySubscriber = false;

    public SubscriberThread(final DataRecorder recorder, final Vector<Object> subscriberVector) {
        this.subscriberVector = subscriberVector;
        this.recorder = recorder;

        final String subscriberDelay = System.getProperty("subscriberDelay");
        if (null != subscriberDelay) {
            subscriberDelayMillis = Long.valueOf(subscriberDelay);
            if (subscriberDelayMillis > 0) {
                delaySubscriber = true;
                logger.debug("Found property for delaying ALL subscribers by " + subscriberDelayMillis + " ms");
            } else {
                logger.error(
                        "Ignoring subscriberDelay.  Value must be greater than 0.  Subscribers will NOT be delayed.");
            }
        }
    }

    @Override
    public void run() {
        try {
            channel = (String) subscriberVector.get(SubscriberTableValues.CHANNEL);
            messagingUtils = (MessagingUtils) subscriberVector.get(SubscriberTableValues.BROKER);

            Boolean isQueue = false;
            if (((String) subscriberVector.get(SubscriberTableValues.TYPE)).equals("Q")) {
                isQueue = true;
            }
            session = messagingUtils.createConsumer(channel, this, isQueue, false);
            subscriberVector.set(SubscriberTableValues.SUBSCRIPTION, session);
        } catch (final JMSException e) {
            logger.error("Error creating consumer.", e);
        }

        channelViewer = (ChannelViewer) subscriberVector.get(SubscriberTableValues.CHANNEL_VIEWER);
        if (channelViewer == null) {
            return;
        }

        subscriberVector.set(SubscriberTableValues.STATUS, SubscriberTableValues.STARTED);
        this.recorder.subscriberTable.repaint();
        fileIsOpen();

    }

    private boolean fileIsOpen() {
        if (isFirstTime) {
            currentTime = System.currentTimeMillis();
            lastTime = currentTime;
            try {
                file = (FileObject) subscriberVector.get(SubscriberTableValues.FILE);
                final String fileName = file.getFullname();
                dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));

                insertStartTimestamp = Boolean.getBoolean("subscriber.insert.start.timestamp");
                startTimeMillis = System.currentTimeMillis();

                isFirstTime = false;
                return true;
            } catch (final Exception e) {
                isFirstTime = false;
                return false;
            }
        } else {
            final FileObject currentFile = (FileObject) subscriberVector.get(SubscriberTableValues.FILE);
            if (!(file.getFullname().equals(currentFile.getFullname()))) {
                try {
                    if (dos != null)
                        // close the old file
                        dos.close();
                } catch (final IOException e1) {
                    logger.error("Error closing file {}", file.getFullname());
                    logger.error("Stacktrace: ", e1);
                }
                file = currentFile;
                currentTime = System.currentTimeMillis();
                lastTime = currentTime;
                try {
                    dos = new DataOutputStream(
                            new BufferedOutputStream(new FileOutputStream(currentFile.getFullname())));
                    return true;
                } catch (final Exception e) {
                    return false;
                }
            } else {
                currentTime = System.currentTimeMillis();
                return true;
            }
        }
    }

    synchronized public void startThread() {
        final String status = (String) subscriberVector.get(SubscriberTableValues.STATUS);
        if (status.equals(SubscriberTableValues.PAUSED)) {
            subscriberVector.set(SubscriberTableValues.STATUS, SubscriberTableValues.STARTED);
            isPaused = false;
            this.notify();
        } else if (status.equals(SubscriberTableValues.STOPPED)) {
            subscriberVector.set(SubscriberTableValues.COUNT, Integer.toString(0));
            this.start();
        }
    }

    public void stopThread() {
        try {
            session.close();
        } catch (final JMSException e) {
            logger.error("Error closing session.", e);
        }

        try {
            dos.close();
        } catch (final NullPointerException ignore) {
            logger.warn("Exception occured closing output stream.", ignore);
        } catch (final Exception e) {
            logger.error("Error closing output stream.", e);
        }

        subscriberVector.set(SubscriberTableValues.STATUS, SubscriberTableValues.STOPPED);
        this.recorder.subscriberTable.repaint();
        isFirstTime = true;
    }

    public void pauseThread() {
        isPaused = true;
        subscriberVector.set(SubscriberTableValues.STATUS, SubscriberTableValues.PAUSED);
        this.recorder.subscriberTable.repaint();
        this.interrupt();
    }

    @Override
    public void onMessage(final Message msg) {
        if (isPaused)
            return;

        // Record
        final byte[] data = getBytes(msg);
        final DataRecorderMessage jsonMessage = new DataRecorderMessage(msg, currentTime);

        final String record = (String) subscriberVector.get(SubscriberTableValues.RECORD);
        if (record.equals(SubscriberTableValues.ON)
                && !((FileObject) subscriberVector.get(SubscriberTableValues.FILE)).getFullname().equals("")) {
            if (fileIsOpen()) {

                if (insertStartTimestamp) {
                    logger.info("Inserting start timestamp: " + file.getFullname());
                    final long diffTimeMillis = System.currentTimeMillis() - startTimeMillis;
                    try {
                        final String emptyString = "Start Timestamp";
                        final byte[] emptyData = emptyString.getBytes();
                        dos.writeLong(diffTimeMillis);
                        final int size = emptyData.length;
                        dos.writeInt(size);
                        dos.write(emptyData);
                    } catch (final IOException e) {
                        logger.error("Error writing to output stream.", e);
                    }
                    insertStartTimestamp = false;
                }

                // ***** normal message processing
                final long waitTime = currentTime - lastTime;
                jsonMessage.setDelayMillis(waitTime);

                try {
                    logger.debug(jsonMessage.toString());
                    dos.write(jsonMessage.toString().getBytes());
                    dos.writeChars("\n");
                } catch (final IOException e1) {
                    logger.error("Error writing to output stream.", e1);
                }

            }
        }

        // Viewer
        if (channelViewer.isVisible()) {
            channelViewer.displayInformationObject(jsonMessage);
        }

        updateMessageCount();

        if (delaySubscriber) {
            try {
                logger.trace("Pausing thread [" + Thread.currentThread().getName() + "] for [" + subscriberDelayMillis
                        + "] ms");
                Thread.currentThread();
                Thread.sleep(subscriberDelayMillis);

            } catch (final InterruptedException e) {
                logger.error("Error sleeping thread.", e);
            }
        }

        try {
            // Acknowledge the message
            msg.acknowledge();
        } catch (final JMSException e) {
            logger.error("Error acknowleding message", e);
        }

    }

    private void updateMessageCount() {
        int i = Integer.parseInt((String) subscriberVector.get(SubscriberTableValues.COUNT));
        i++;
        subscriberVector.set(SubscriberTableValues.COUNT, Integer.toString(i));
        this.recorder.subscriberTable.repaint();
    }

    /**
     * Read the the payload of the message and return it in a byte array.
     * 
     * @param msg
     * @return
     */
    private byte[] getBytes(final Message msg) {
        byte[] data = null;
        try {
            if (msg instanceof BytesMessage) {
                final BytesMessage tmp = (BytesMessage) msg;
                int len;
                len = (int) ((BytesMessage) msg).getBodyLength();
                data = new byte[len];
                tmp.readBytes(data);
            } else if (msg instanceof TextMessage) {
                data = ((TextMessage) msg).getText().getBytes();

            }
        } catch (final JMSException e) {
            logger.error("Error getting bytes from message.", e);
        }
        return data;
    }

    /**
     * Constructs a json message lookling like:
     * 
     * 
     * @param msg
     */
    public void constructJson(final Message msg) {

    }

}