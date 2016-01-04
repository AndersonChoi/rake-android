package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;
public final class ServerResponseMetric {

    private ServerResponseMetric() {}

    private Status flushStatus;
    private String responseBody;
    private int responseCode;
    private long serverResponseTime;
    private FlushMethod flushMethod;
    private Throwable throwable;

    private ServerResponseMetric(String responseBody,
                                int responseCode,
                                long serverResponseTime,
                                FlushMethod flushMethod) {
        this.responseBody = responseBody;
        this.responseCode = responseCode;
        this.serverResponseTime = serverResponseTime;
        this.flushMethod = flushMethod;
    }

    public ServerResponseMetric(Throwable e, Status flushStatus, FlushMethod flushMethod) {
        this.throwable = e;
        this.flushStatus = flushStatus;
        this.flushMethod = flushMethod;
    }

    /** static */

    public static ServerResponseMetric create(String responseBody,
                                              int responseCode,
                                              long serverResponseTime,
                                              FlushMethod flushMethod) {

        /** 오류가 아니라 body 는 파싱 실패시 NULL 일 수 있으므로 result 만 검사하도록 작성 */
        return new ServerResponseMetric(
                responseBody, responseCode, serverResponseTime, flushMethod);
    }

    public static ServerResponseMetric createErrorMetric(Throwable e,
                                                         Status flushStatus,
                                                         FlushMethod flushMethod) {
        if (null == flushStatus) return null;

        return new ServerResponseMetric(e, flushStatus, flushMethod);
    }

    /** instance */

    public ServerResponseMetric setFlushStatus(Status flushStatus) { this.flushStatus = flushStatus; return this; }

    public Status getFlushStatus () { return flushStatus; }
    public String getResponseBody() { return responseBody; }
    public int getResponseCode() { return responseCode; }
    public long getServerResponseTime() { return serverResponseTime; }
    public Throwable getExceptionInfo() { return throwable; }
    public FlushMethod getFlushMethod() { return flushMethod; }

}
