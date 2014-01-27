package com.welty.novello.coca;

public enum OperatingSystem {
    WINDOWS, MACINTOSH, LINUX, UNKNOWN;

    public static final OperatingSystem os = detectOs();

    private static OperatingSystem detectOs() {
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            return MACINTOSH;
        }
        else {
            return UNKNOWN;
        }
    }
}
