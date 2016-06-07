package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;

public final class ServerResponse {

    private ServerResponse() {}

    private Status flushStatus;
    private String responseBody;
    private int responseCode;
    private long serverResponseTime;
    private FlushMethod flushMethod;
    private Throwable throwable;
    private boolean isErrorResponse = false;

    private ServerResponse(String responseBody,
                           int responseCode,
                           long serverResponseTime,
                           FlushMethod flushMethod) {
        this.responseBody = responseBody;
        this.responseCode = responseCode;
        this.serverResponseTime = serverResponseTime;
        this.flushMethod = flushMethod;
        this.isErrorResponse = false;
    }

    public ServerResponse(Throwable e,
                          Status flushStatus,
                          FlushMethod flushMethod) {

        this.throwable = e;
        this.flushStatus = flushStatus;
        this.flushMethod = flushMethod;
        this.isErrorResponse = true;
    }

    /** static */

    public static ServerResponse create(String responseBody,
                                        int responseCode,
                                        long serverResponseTime,
                                        FlushMethod flushMethod) {

        /** 오류가 아니라 body 는 파싱 실패시 NULL 일 수 있으므로 result 만 검사하도록 작성 */
        return new ServerResponse(
                responseBody,
                responseCode,
                serverResponseTime,
                flushMethod
        );
    }

    public static ServerResponse createErrorResponse(Throwable e,
                                                     Status flushStatus,
                                                     FlushMethod flushMethod) {
        if (null == flushStatus) return null;

        return new ServerResponse(e, flushStatus, flushMethod);
    }

    /** instance */

    public ServerResponse setFlushStatus(Status flushStatus) { this.flushStatus = flushStatus; return this; }
    public boolean isErrorResponse() { return isErrorResponse; }

    public Status getFlushStatus () { return flushStatus; }
    public String getResponseBody() { return responseBody; }
    public int getResponseCode() { return responseCode; }
    public long getServerResponseTime() { return serverResponseTime; }
    public Throwable getExceptionInfo() { return throwable; }
    public FlushMethod getFlushMethod() { return flushMethod; }

}
