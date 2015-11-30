package com.rake.android.rkmetrics.metric.model;

public enum Status {
    DONE("done"),
    ERROR("error"),
    FAIL("fail"),
    DROP("drop"),
    RETRY("retry");

    private String value;
    public String getValue() { return value; }

    Status(String value) {
        this.value = value;
    }
}
