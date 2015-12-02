package com.rake.android.rkmetrics.metric.model;

public enum FlushType {

    MANUAL_FLUSH("MANUAL_FLUSH"),
    AUTO_FLUSH_BY_TIMER("AUTO_FLUSH_BY_TIMER"),
    AUTO_FLUSH_BY_COUNT("AUTO_FLUSH_BY_COUNT"),
    AUTO_FLUSH_BY_RETRY("AUTO_FLUSH_BY_RETRY");

    private String value;
    public String getValue() { return value; }

    FlushType(String value) {
        this.value = value;
    }
}
