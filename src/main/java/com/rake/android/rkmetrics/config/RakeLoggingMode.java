package com.rake.android.rkmetrics.config;

public enum RakeLoggingMode {
    NO("NO"), YES("YES");

    private String loggingMode;
    RakeLoggingMode(String loggingMode) { this.loggingMode = loggingMode; }
    @Override public String toString() { return loggingMode; }
}
