package com.rake.android.rkmetrics.network;

public enum TransmissionResult {
    SUCCESS("SUCCESS"),
    FAILURE_RECOVERABLE("FAILURE_RECOVERABLE"),
    FAILURE_UNRECOVERABLE("FAILURE_UNRECOVERABLE");

    private String value;
    public String getValue() { return value; }
    TransmissionResult(String value) { this.value= value; }
    @Override public String toString() { return value; }
}

