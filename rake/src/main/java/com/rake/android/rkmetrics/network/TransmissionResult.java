package com.rake.android.rkmetrics.network;

public enum TransmissionResult {
    NONE("NONE"),
    SUCCESS("SUCCESS"),
    FAILURE_EMPTY_TABLE("FAILURE_EMPTY_TABLE"),
    FAILURE_INVALID_ARGUMENT("FAILURE_INVALID_ARGUMENT"),
    FAILURE_RECOVERABLE("FAILURE_RECOVERABLE"),
    FAILURE_UNRECOVERABLE("FAILURE_UNRECOVERABLE");

    private String result;
    TransmissionResult(String result) { this.result = result; }
    @Override public String toString() { return result; }
}

