package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;

public final class ServerResponseMetric {

    private ServerResponseMetric() {}

    private Status flushStatus;
    private String responseBody;
    private int responseCode;
    private long serverResponseTime;
    private Throwable throwable;

    public ServerResponseMetric(String responseBody,
                                int responseCode,
                                long serverResponseTime) {
        this.responseBody = responseBody;
        this.responseCode = responseCode;
        this.serverResponseTime = serverResponseTime;
    }

    public ServerResponseMetric(Throwable e, Status flushStatus) {
        this.throwable = e;
        this.flushStatus = flushStatus;
    }

    /** static */

    public static ServerResponseMetric create(String responseBody,
                                              int responseCode,
                                              long serverResponseTime) {

        /** 오류가 아니라 body 는 파싱 실패시 NULL 일 수 있으므로 result 만 검사하도록 작성 */
        return new ServerResponseMetric(responseBody, responseCode, serverResponseTime);
    }

    public static ServerResponseMetric createErrorMetric(Throwable e, Status flushStatus) {
        if (null == flushStatus) return null;

        return new ServerResponseMetric(e, flushStatus);
    }

    /** instance */

    public ServerResponseMetric setThrowable(Throwable e) { this.throwable = e; return this; }
    public ServerResponseMetric setFlushStatus(Status flushStatus) { this.flushStatus = flushStatus; return this; }

    public Status getFlushStatus () { return flushStatus; }
    public String getResponseBody() { return responseBody; }
    public int getResponseCode() { return responseCode; }
    public long getServerResponseTime() { return serverResponseTime; }
    public Throwable getExceptionInfo() { return throwable; }

}
