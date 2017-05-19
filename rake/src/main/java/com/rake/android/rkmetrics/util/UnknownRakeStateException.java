package com.rake.android.rkmetrics.util;

/**
 * Rake Client 내부에서 IllegalState 를 표현하기 위한 예외
 */
public final class UnknownRakeStateException extends RuntimeException {
    public UnknownRakeStateException(String message) {
        super(message);
    }
}
