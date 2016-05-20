package com.datarecorder;

public class SubscriberTableValues {

    public final static int STATUS = 0;
    public final static int COUNT = 1;
    public final static int BROKER = 2;
    public final static int CHANNEL = 3;
    public final static int TYPE = 4;
    public final static int FILE = 5;
    public final static int RECORD = 6;
    public final static int SUBSCRIPTION = 7;
    public final static int INFORMATION = 8;
    public final static int CHANNEL_VIEWER = 9;
    public final static int SUBSCRIBER_THREAD = 10;

    public final static String STARTED = "Started";
    public final static String PAUSED = "Paused";
    public final static String STOPPED = "Stopped";
    public final static String RESET = "0";
    public final static String ON = "On";
    public final static String OFF = "Off";

    public final static String[] columnNames = { "Status", "Count", "Broker", "T/Q Name", "Type", "File", "Record" };

    public final static Object[][] data = {};
}
