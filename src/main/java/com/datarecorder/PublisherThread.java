package com.datarecorder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;

class PublisherThread extends Thread {
    private static final String MESSAGE_TAG = "@Message";
    private static final String DOCUMENT_TAG = "@Document";
    private static final String TIME_TAG = "@Time";
    private static final int FILE_NOT_FOUND = 0;
    private static final int DR1_FORMAT = 1;
    private static final int DR2_FORMAT = 2;
    private static final int ARENA_FORMAT = 3;
    private static final int UNKNOWN_FORMAT = 4;
    private static final int DR3_FORMAT = 5;

    private DataRecorder recorder;
    private Vector<Object> v;
    private String file;
    private ChannelPublisher pubClass;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean messageFound = false;

    private boolean suppressStartTimestamp;
    private int messageCount;
    private int dr3FileIndex;

    private int format;
    private long lastTime;
    private FileInputStream fis;
    private ObjectInputStream ois;
    private DataInputStream dis;
    private BufferedReader br;
    private String dr1Message;
    private byte[] dr2Message;
    private DataRecorderMessage dr3Message;
    private String arenaMessage;
    private long waitTime;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy DDD HH:mm:ss.S");
    final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private List<String> lines;

    private Logger logger = LoggingObject.getLogger(this.getClass());

    public PublisherThread(final DataRecorder recorder, final Vector<Object> v) {
        this.v = v;
        this.recorder = recorder;
        file = ((FileObject) v.get(PublisherTableValues.FILE)).getFullname();
        pubClass = (ChannelPublisher) v.get(PublisherTableValues.CHANNEL_PUBLISHER);

        format = getFormat();
        if (format == UNKNOWN_FORMAT) {
            JOptionPane.showMessageDialog(recorder, "Unknown file format: " + file, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void run() {
        if (format == UNKNOWN_FORMAT || format == FILE_NOT_FOUND) {
            return;
        }

        openFile();
        lastTime = 0;
        waitTime = 0;
        v.set(PublisherTableValues.STATUS, PublisherTableValues.STARTED);
        v.set(PublisherTableValues.COUNT, Integer.toString(0));

        this.recorder.publisherTable.repaint();
        if (format != ARENA_FORMAT) {
            loadNextMessage();
        }

        while (isStarted) {
            try {
                // Pause
                if (isPaused) {
                    synchronized (this) {
                        v.set(PublisherTableValues.STATUS, PublisherTableValues.PAUSED);
                        this.recorder.publisherTable.repaint();
                        while (isPaused) {
                            wait();
                        }
                        v.set(PublisherTableValues.STATUS, PublisherTableValues.STARTED);
                        this.recorder.publisherTable.repaint();
                    }
                }
                // Publish
                if (messageFound) {
                    switch (format) {
                    case DR1_FORMAT:
                        pubClass.publish(dr1Message.getBytes());
                        break;
                    case DR2_FORMAT:
                        messageCount++;
                        if (suppressStartTimestamp && messageCount == 1) {
                            logger.info("Suppressing start timestamp: " + file);
                        } else {
                            pubClass.publish(dr2Message);
                        }
                        break;
                    case DR3_FORMAT:
                        pubClass.publish(dr3Message);
                        break;
                    case ARENA_FORMAT:
                        pubClass.publish(arenaMessage.getBytes());
                        break;

                    default:
                        break;
                    }
                }

                // Wait
                loadNextMessage();
                if (messageFound) {
                    final RateObject rate = (RateObject) v.get(PublisherTableValues.RATE);
                    final int value = rate.getValue();
                    final String name = rate.getName();
                    try {
                        if (name.equals(RateObject.ACTUAL)) {
                            Thread.sleep(waitTime);
                        } else if (name.equals(RateObject.FASTER)) {
                            Thread.sleep(waitTime / value);
                        } else if (name.equals(RateObject.SLOWER)) {
                            Thread.sleep(waitTime * value);
                        } else if (name.equals(RateObject.CONSTANT)) {
                            Thread.sleep(1000 / value);
                        }
                    } catch (final InterruptedException wakeUp) {
                        logger.warn("Exception while sleeping between message sends.");
                    }
                }

                // Loop
                if (!messageFound) {
                    final String loop = (String) v.get(PublisherTableValues.LOOP);
                    if (loop.equals(PublisherTableValues.OFF)) {
                        stopThread();
                    } else {
                        openFile();
                        lastTime = 0;
                        waitTime = 0;
                        v.set(PublisherTableValues.STATUS, PublisherTableValues.STARTED);
                        loadNextMessage();
                    }
                }
            } catch (final InterruptedException ex) {
                v.set(PublisherTableValues.STATUS, PublisherTableValues.STOPPED);
                this.recorder.publisherTable.repaint();
            } catch (final Exception ex) {
                logger.error("Error publishing message.", ex);
            }
        }
        isStarted = false;
        v.set(PublisherTableValues.STATUS, PublisherTableValues.STOPPED);
        this.recorder.publisherTable.repaint();
    }

    synchronized public void startThread() {
        final String status = (String) v.get(PublisherTableValues.STATUS);
        if (status.equals(PublisherTableValues.PAUSED)) {
            isPaused = false;
            this.notify();
        } else if (status.equals(PublisherTableValues.STOPPED)) {
            isStarted = true;
            this.start();
        }
    }

    public void stopThread() {
        isStarted = false;
        this.interrupt();
    }

    synchronized public void pauseThread() {
        isPaused = true;
        this.interrupt();
    }

    private void loadNextMessage() {

        switch (format) {
        case DR1_FORMAT:
            loadDR1Message();
            break;
        case DR2_FORMAT:
            loadDR2Message();
            break;
        case DR3_FORMAT:
            loadDR3Message();
            break;
        case ARENA_FORMAT:
            loadArenaMessage();
            break;
        default:
            break;
        }
    }

    private void loadDR1Message() {

        try {
            while ((dr1Message = br.readLine()) != null) {
                if (dr1Message.startsWith(TIME_TAG)) {
                    String temp;
                    temp = dr1Message.substring(TIME_TAG.length() + 1);
                    final Date date = formatter.parse(temp.trim());
                    final long currTime = date.getTime();
                    waitTime = currTime - lastTime;
                    if (waitTime < 0)
                        waitTime = 0;
                    lastTime = currTime;
                    break;
                }
            }
            while ((dr1Message = br.readLine()) != null) {
                if (dr1Message.startsWith(DOCUMENT_TAG)) {
                    dr1Message = br.readLine();
                    messageFound = true;
                    break;
                }
            }
            if (dr1Message == null) {
                messageFound = false;
            }
        } catch (final IOException e) {
            logger.error("Error reading file.", e);
        } catch (final ParseException e) {
            logger.error("Error parsing date.", e);
        }

    }

    private void loadDR2Message() {
        try {
            waitTime = dis.readLong();
            if (waitTime < 0)
                waitTime = 0;
            final int size = dis.readInt();
            dr2Message = new byte[size];
            dis.readFully(dr2Message);
            messageFound = true;
        } catch (final EOFException e) {
            messageFound = false;
            logger.error("Read through file and didn't find what we are looking for.", e);
        } catch (final Exception e) {
            logger.error("Error reading file.", e);
        }
    }

    private void loadDR3Message() {

        try {
            if (dr3FileIndex < lines.size()) {
                dr3Message = OBJECT_MAPPER.readValue(lines.get(dr3FileIndex), DataRecorderMessage.class);
                waitTime = dr3Message.getDelayMillis();
                //System.out.println("waitTime: " + waitTime);
                dr3FileIndex++;
                messageFound = true;
            } else {
                messageFound = false;
                logger.debug("Reached end of file.");
            }
        } catch (final IOException e1) {
            messageFound = false;
            logger.error("Error reading line from playback file.");
            logger.error("Stacktrace.", e1);
        }

    }

    private void loadArenaMessage() {
        try {
            waitTime = 0;
            lastTime = 0;
            messageFound = true;
            arenaMessage = (String) ois.readObject();
        } catch (final EOFException e) {
            messageFound = false;
            logger.error("Read through file and didn't find what we are looking for.", e);
        } catch (final Exception e) {
            logger.error("Error reading file.", e);
        }
    }

    private int getFormat() {

        // Check DR3_FORMAT
        if (FileUtils.getFile(file).isFile()) {
            final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
            try {
                lines = FileUtils.readLines(new File(file));

                if (!lines.isEmpty()) {
                    final DataRecorderMessage drm = OBJECT_MAPPER.readValue(lines.get(0), DataRecorderMessage.class);
                    if (drm.toString() != null) {
                        logger.debug("Capture file is a DataRecorder 3 format.");
                        return DR3_FORMAT;
                    } else {
                        // Not a DR2 format
                    }
                }
            } catch (final IOException e) {
                return UNKNOWN_FORMAT;
            }
        }

        // Check DR1_FORMAT
        if (openFile()) {
            try {
                final byte[] b = new byte[MESSAGE_TAG.length()];
                dis.readFully(b);
                final String test = new String(b);
                if (test.equals(MESSAGE_TAG)) {
                    logger.debug("Capture file is a DataRecorder 1 format.");
                    return DR1_FORMAT;
                } else {
                    // not DR1 format
                }
            } catch (final IOException e) {
                return UNKNOWN_FORMAT;
            }
        } else {
            return FILE_NOT_FOUND;
        }

        // Check DR2_FORMAT
        if (openFile()) {
            try {
                // This dis.readLong(); call must be here to read off part of the message.
                // Otherwise you will get an error about the file not being a correct file.
                final long whatIsThis = dis.readLong();
                final int size = dis.readInt();
                if (size > 0) {
                    final byte[] data = new byte[size];
                    dis.readFully(data);
                    logger.debug("Capture file is a DataRecorder 2 format.");
                    return DR2_FORMAT;
                } else {
                    // not DR2 format
                }
            } catch (final OutOfMemoryError e) {
                // not DR2 format
                logger.error("Not a DataRecorder 2.0 file format.", e);
            } catch (final IOException e) {
                logger.error("Error reading file.", e);
                return UNKNOWN_FORMAT;
            }
        } else {
            return FILE_NOT_FOUND;
        }

        // Check Arena
        if (openFile()) {
            try {
                ois = new ObjectInputStream(fis);
                arenaMessage = (String) ois.readObject();
                logger.debug("Capture file is an Arena format.");
                return ARENA_FORMAT;
            } catch (final Exception e) {
                // not Arena format
                logger.error("Not an Arena file format.", e);
                return UNKNOWN_FORMAT;
            }
        } else {
            return FILE_NOT_FOUND;
        }

    }

    private boolean openFile() {
        try {
            fis = null;
            dis = null;
            br = null;
            fis = new FileInputStream(file);
            dis = new DataInputStream(new BufferedInputStream(fis));
            br = new BufferedReader(new InputStreamReader(dis));

            suppressStartTimestamp = Boolean.getBoolean("publisher.suppress.start.timestamp");
            
            messageCount = 0;
            dr3FileIndex = 0;
        } catch (final FileNotFoundException e) {
            JOptionPane.showMessageDialog(recorder, "Publisher input file not found: " + file + 
            		"\nYou can't start this Publisher without a valid input file." +
            		"\nYou can however still inject messages via right-click View Injector... on this Publisher.", "Publisher Start Error", JOptionPane.ERROR_MESSAGE);
            // No need to send error to log... user was informed above (mwt)
            //logger.error("File not found: " + file, e);
            return false;
        }
        return true;
    }

 
    // public synchronized void publish(byte[] bytes) throws Exception {
    // topicPublisher.send(topicSession.createTextMessage(new String(bytes)));
    // // count
    // int i = Integer.parseInt((String)v.get(PublisherTableValues.COUNT));
    // i++;
    // v.set(PublisherTableValues.COUNT, Integer.toString(i));
    // // display
    // if(cv!=null && cv.isShowing())
    // cv.displayInformationObject(bytes);
    // // repaint
    // this.recorder.publisherTable.repaint();
    // }

    // public String getPublicationTypeName() {
    // return info.getInformationType().getName();
    // }
    //
}