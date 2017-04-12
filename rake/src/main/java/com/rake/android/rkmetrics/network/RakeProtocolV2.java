package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.util.Logger;

import java.util.Locale;

public final class RakeProtocolV2 {
    static final String RAKE_PROTOCOL_VERSION = "V2";
    static final String CHAR_ENCODING = "UTF-8";

    static final int HTTP_STATUS_CODE_OK = 200;
    static final int HTTP_STATUS_CODE_REQUEST_TOO_LONG = 413;
    static final int HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
    static final int HTTP_STATUS_CODE_BAD_GATEWAY = 502;
    static final int HTTP_STATUS_CODE_SERVICE_UNAVAILABLE = 503;

    /**
     * RakeProtocolV2 의 HTTP 응답 규격을 정의
     *
     * @param responseCode 200 (OK) 일 경우만 성공적으로 서버에서 처리했음을 나타냄. (Protocol V2 에서 body 는 검증하지 않음)
     * @return Status (DONE / RETRY / DROP)
     */
    static Status interpretResponse(int responseCode) {
        Status status;

        switch (responseCode) {
            case HTTP_STATUS_CODE_OK:
                return Status.DONE; // Successful status. So return Status.DONE immediately.

            case HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR: /* UNKNOWN FAILURE */
            case HTTP_STATUS_CODE_BAD_GATEWAY:           /* TOMCAT FAILURE */
            case HTTP_STATUS_CODE_SERVICE_UNAVAILABLE:   /* NGINX FAILURE */
                status = Status.RETRY;
                break;
            case HTTP_STATUS_CODE_REQUEST_TOO_LONG:
            default:
                status = Status.DROP;
        }

        Logger.e("Server returned negative response. make sure that your token is valid");
        return status;
    }

    public static void reportResponse(String responseBody, int responseCode) {
        String message = String.format(Locale.US, "[NETWORK] Server returned code: %d, body: %s", responseCode, responseBody);
        Logger.t(message);
    }
}
