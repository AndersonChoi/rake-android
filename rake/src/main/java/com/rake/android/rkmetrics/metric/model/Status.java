package com.rake.android.rkmetrics.metric.model;

public enum Status {
    DONE("DONE"),
    ERROR("ERROR"),
    FAIL("FAIL"),
    DROP("DROP"),
    RETRY("RETRY"),
    UNKNOWN("UNKNOWN");

    private String value;
    public String getValue() { return value; }

    Status(String value) {
        this.value = value;
    }
}
