package com.rake.android.rkmetrics.metric;

public enum Action {

    EMPTY(""),
    INSTALL("install"),
    CONFIGURE("configure"),
    TRACK("track"),
    FLUSH("flush");

    private String value;
    public String getValue() { return value; }

    Action(String value) {
        this.value = value;
    }
}
