package com.rake.android.rkmetrics.metric.model;

public enum FlushResult {
    FAILURE_EMPTY_TABLE("FAILURE_EMPTY_TABLE"),
    FAILURE_INVALID_ARGUMENT("FAILURE_INVALID_ARGUMENT"),
    FAILURE_RECOVERABLE("FAILURE_RECOVERABLE"),
    FAILURE_UNRECOVERABLE("FAILURE_UNRECOVERABLE"),
    FAILURE_UNKNOWN("FAILURE_UNKNOWN"),
    SUCCESS("SUCCESS");

    private String value;
    public String getValue() { return value; }

    FlushResult(String value) {
        this.value = value;
    }
}
