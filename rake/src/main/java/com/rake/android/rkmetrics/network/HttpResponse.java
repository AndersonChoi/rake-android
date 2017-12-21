package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.util.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

/**
 * Created by 1000731 on 2017. 12. 20..
 */

public class HttpResponse {
    private static final int HTTP_STATUS_CODE_OK = 200;
    private static final int HTTP_STATUS_CODE_REQUEST_TOO_LONG = 413;
    private static final int HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
    private static final int HTTP_STATUS_CODE_BAD_GATEWAY = 502;
    private static final int HTTP_STATUS_CODE_SERVICE_UNAVAILABLE = 503;

    private Status flushStatus;
    private int responseCode;
    private String responseBody;
    private long responseTime;
    private Throwable exception;

    HttpResponse(int responseCode, String responseBody, long responseTime) {
        this.responseCode = responseCode;
        this.responseBody = responseBody;
        this.responseTime = responseTime;

        // define flush status
        switch (this.responseCode) {
            case HTTP_STATUS_CODE_OK:
                flushStatus = Status.DONE; // Successful status. So return Status.DONE immediately.

            case HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR: // UNKNOWN FAILURE
            case HTTP_STATUS_CODE_BAD_GATEWAY:           // TOMCAT FAILURE
            case HTTP_STATUS_CODE_SERVICE_UNAVAILABLE:   // NGINX FAILURE
                flushStatus = Status.RETRY;

            case HTTP_STATUS_CODE_REQUEST_TOO_LONG:
            default:
                flushStatus = Status.DROP;
        }
    }

    HttpResponse(Throwable exception) {
        this.exception = exception;

        // define flush status
        if (exception instanceof UnsupportedEncodingException) {
            Logger.e("Invalid encoding", exception);
            flushStatus = Status.DROP;
        } else if (exception instanceof MalformedURLException) {
            Logger.e("Malformed url (DROP)", exception);
            flushStatus = Status.DROP;
        } else if (exception instanceof ProtocolException) {
            Logger.e("Invalid protocol (DROP)", exception);
            flushStatus = Status.DROP;
        } else if (exception instanceof IOException) {
            Logger.e("Can't post message to Rake Server (RETRY)", exception);
            flushStatus = Status.RETRY;
        } else if (exception instanceof OutOfMemoryError) {
            Logger.e("Can't post message to Rake Server (RETRY)", exception);
            flushStatus = Status.RETRY;
        } else if (exception instanceof Exception) {
            Logger.e("Uncaught exception (DROP)", exception);
            flushStatus = Status.DROP;
        } else {
            Logger.e("Uncaught throwable (DROP)", exception);
            flushStatus = Status.DROP;
        }
    }

    public Status getFlushStatus() {
        return flushStatus;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isErrorResponse() {
        return exception != null;
    }
}
