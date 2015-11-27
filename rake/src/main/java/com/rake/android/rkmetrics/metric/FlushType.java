package com.rake.android.rkmetrics.metric;

public enum FlushType {

    MANUAL_FLUSH("MANUAL_FLUSH"),
    AUTO_FLUSH_BY_TIMER("AUTO_FLUSH_BY_TIMER"),
    AUTO_FLUSH_BY_COUNT("AUTO_FLUSH_BY_COUNT");

    private String value;
    public String getValue() { return value; }

    FlushType(String value) {
        this.value = value;
    }
}
