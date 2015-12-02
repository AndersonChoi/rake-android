package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.FlushResult;

public final class ServerResponseMetric {

    private ServerResponseMetric() {}

    FlushResult flushResult;
    String responseBody;
    int responseCode;
    long serverResponseTime;
    Throwable e;

    public ServerResponseMetric(Throwable e,
                                FlushResult flushResult,
                                String responseBody,
                                int responseCode,
                                long serverResponseTime) {

        this.e = e;
        this.flushResult = flushResult;
        this.responseBody = responseBody;
        this.responseCode = responseCode;
        this.serverResponseTime = serverResponseTime;
    }

    public FlushResult getFlushResult() { return flushResult; }
    public String getResponseBody() { return responseBody; }
    public int getResponseCode() { return responseCode; }
    public long getServerResponseTime() { return serverResponseTime; }
    public Throwable getExceptionInfo() { return e; }

    public static ServerResponseMetric create(Throwable e,
                                              FlushResult flushResult,
                                              String responseBody,
                                              int responseCode,
                                              long serverResponseTime) {

        /** 오류가 아니라 body 는 파싱 실패시 NULL 일 수 있으므로 result 만 검사하도록 작성 */
        if (null == flushResult) return null;

        return new ServerResponseMetric(
                e, flushResult, responseBody, responseCode, serverResponseTime);
    }
}
