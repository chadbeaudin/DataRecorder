package com.datarecorder;

public class RateObject {

    public static final String ACTUAL = "Actual";
    public static final String FASTER = "Faster";
    public static final String SLOWER = "Slower";
    public static final String CONSTANT = "Constant";

    private String name;
    private int value;
    private String suffix;

    public RateObject(final String name) {
        this.name = name;

        if (name.equals(ACTUAL)) {
            suffix = "";
        }

        if (name.equals(FASTER)) {
            suffix = "x";
        }

        if (name.equals(SLOWER)) {
            suffix = "x";
        }

        if (name.equals(CONSTANT)) {
            suffix = "/s";
        }
    }

    public RateObject(final String name, final int value) {
        this(name);
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public String getSuffix() {
        return suffix;
    }

    public static String parseName(final String input) {
        final String[] array = input.split(" ");
        return array[0];
    }

    public static int parseValue(final String input) {
        final String[] array = input.split(" ");
        if (array.length < 2) {
            return 0;
        }

        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array[1].length(); i++) {
            final char c = array[1].charAt(i);
            if (Character.isDigit(c)) {
                buffer.append(c);
            }
        }

        if (buffer.length() > 0) {
            return Integer.parseInt(buffer.toString());
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        if (name.equals(ACTUAL)) {
            return name;
        } else {
            return name + " " + value + suffix;
        }
    }
}
