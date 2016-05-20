package com.datarecorder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;

public class PublisherStatistics {

    private String file;
    private int count;
    private DataInputStream dis;
    private BufferedReader br;
    private String dr1Message;
    private byte[] dr2Message;
    private static final String MESSAGE_TAG = "@Message";
    private static final String DR1_FORMAT = "TD1_FORMAT";
    private static final String DR2_FORMAT = "TD2_FORMAT";

    private Logger logger = LoggingObject.getLogger(this.getClass());

    public PublisherStatistics(final String file) {
        this.file = file;
        final String format = getFormat();
        if (format != null) {
            openFile();
            if (format.equals(DR1_FORMAT)) {
                calcTd1();
            } else if (format.equals(DR2_FORMAT)) {
                calcTd2();
            }
        } else {
            // MessageBox.showError(getParent(), "Unrecongized message format");
        }
    }

    public int getCount() {
        return count;
    }

    private void calcTd1() {
        try {
            while ((dr1Message = br.readLine()) != null) {
                if (dr1Message.startsWith(MESSAGE_TAG)) {
                    count++;
                }
            }
        } catch (final Exception e) {
            logger.error("Error reading from message.", e);
        }
    }

    private void calcTd2() {
        boolean more = true;
        while (more) {
            try {
                final int size = dis.readInt();
                dr2Message = new byte[size];
                dis.readFully(dr2Message);
                count++;
            } catch (final EOFException e) {
                more = false;
            } catch (final IOException e) {
                logger.error("Error reading from message.", e);
            }
        }
    }

    private String getFormat() {

        // Check DR1_FORMAT
        openFile();

        final byte[] b = new byte[MESSAGE_TAG.length()];
        final String test = new String(b);
        if (test.equals(MESSAGE_TAG)) {
            return DR1_FORMAT;
        }

        // Check DR2_FORMAT
        openFile();
        try {
            final int size = dis.readInt();
            final byte[] data = new byte[size];
            dis.readFully(data);
            return DR2_FORMAT;
        } catch (final OutOfMemoryError e) {
            // not DR2 format
            // remove this if you add others below!!!
            return null;
        } catch (final IOException e) {
            // remove this if you add others below!!!
            return null;
        }

        // Others someday...
    }

    private void openFile() {
        try {
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            br = new BufferedReader(new InputStreamReader(dis));
        } catch (final FileNotFoundException e) {
            logger.error("Error opening file: {}", file);
            logger.error("Stacktrace: ", e);
        }
    }
}
