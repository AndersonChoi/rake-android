package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;

public final class ServerResponse {

    private Status flushStatus;
    private String responseBody;
    private int responseCode;
    private long serverResponseTime;
    private Throwable throwable;
    private boolean isErrorResponse = false;

    private ServerResponse(String responseBody, int responseCode, long serverResponseTime) {
        this.responseBody = responseBody;
        this.responseCode = responseCode;
        this.serverResponseTime = serverResponseTime;
        this.isErrorResponse = false;
    }

    private ServerResponse(Throwable e, Status flushStatus) {
        this.throwable = e;
        this.flushStatus = flushStatus;
        this.isErrorResponse = true;
    }

    /*
     * static
     */
    public static ServerResponse create(String responseBody, int responseCode, long serverResponseTime) {

        /* 오류가 아니라 body 는 파싱 실패시 NULL 일 수 있으므로 result 만 검사하도록 작성 */
        return new ServerResponse(responseBody, responseCode, serverResponseTime);
    }

    static ServerResponse createErrorResponse(Throwable e, Status flushStatus) {
        if (flushStatus == null) {
            return null;
        }

        return new ServerResponse(e, flushStatus);
    }

    /*
        Getters
    */
    public ServerResponse setFlushStatus(Status flushStatus) {
        this.flushStatus = flushStatus;
        return this;
    }

    public boolean isErrorResponse() {
        return isErrorResponse;
    }

    public Status getFlushStatus() {
        return flushStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public long getServerResponseTime() {
        return serverResponseTime;
    }

    public Throwable getExceptionInfo() {
        return throwable;
    }

    public String getFlushMethod() {
        // (2017.04) Rake-Android 사용 앱들의 Froyo(Android 2.2, versionCode = 8) 이하 단말 지원 종료.
        // 따라서 apache HttpClient 사용을 제거하고 HttpURLConnection으로 network 요청 통일. (Google 권장 사항)
        return "HTTP_URL_CONNECTION";
    }
}
