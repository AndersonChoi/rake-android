package com.rake.android.rkmetrics.network;

public final class ServerResponseMetric {

    private ServerResponseMetric() {}

    TransmissionResult transmissionResult;
    String responseBody;
    int responseCode;
    long serverResponseTime;
    Throwable e;

    public ServerResponseMetric(Throwable e,
                                TransmissionResult transmissionResult,
                                String responseBody,
                                int responseCode,
                                long serverResponseTime) {

        this.e = e;
        this.transmissionResult = transmissionResult;
        this.responseBody = responseBody;
        this.responseCode = responseCode;
        this.serverResponseTime = serverResponseTime;
    }

    public TransmissionResult getTransmissionResult() { return transmissionResult; }
    public String getResponseBody() { return responseBody; }
    public int getResponseCode() { return responseCode; }
    public long getServerResponseTime() { return serverResponseTime; }
    public Throwable getExceptionInfo() { return e; }

    public static ServerResponseMetric create(Throwable e,
                                              TransmissionResult transmissionResult,
                                              String responseBody,
                                              int responseCode,
                                              long serverResponseTime) {

        /** 오류가 아니라 body 는 파싱 실패시 NULL 일 수 있으므로 transmissionResult 만 검사하도록 작성 */
        if (null == transmissionResult) return null;

        return new ServerResponseMetric(
                e, transmissionResult, responseBody, responseCode, serverResponseTime);
    }
}
