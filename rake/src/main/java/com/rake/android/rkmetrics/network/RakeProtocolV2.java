package com.rake.android.rkmetrics.network;

import static com.rake.android.rkmetrics.metric.model.Status.*;
import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.util.Logger;

import java.util.Locale;

public final class RakeProtocolV2 {
    public static final String RAKE_PROTOCOL_VERSION = "V2";
    public static final String CHAR_ENCODING = "UTF-8";

    public static final int HTTP_STATUS_CODE_OK = 200;
    public static final int HTTP_STATUS_CODE_REQUEST_TOO_LONG = 413;
    public static final int HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
    public static final int HTTP_STATUS_CODE_BAD_GATEWAY = 502;
    public static final int HTTP_STATUS_CODE_SERVICE_UNAVAILABLE = 503;

    public static Status interpretResponseCode(int code) {
        switch (code) {
            case HTTP_STATUS_CODE_REQUEST_TOO_LONG: return Status.DROP;
            case HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR: /* UNKNOWN FAILURE */
            case HTTP_STATUS_CODE_BAD_GATEWAY:           /* TOMCAT FAILURE */
            case HTTP_STATUS_CODE_SERVICE_UNAVAILABLE:   /* NGINX FAILURE */
                return Status.RETRY;
            default: return Status.DROP;
        }
    }

    public static boolean isValidResponseCode(int code) {
        /**
         * HttpURLConnection.HTTP_OK 와 HttpStatus.SC_OK 둘다 200 이므로 값으로 200 을 사용
         */

        return code == HTTP_STATUS_CODE_OK;
    }

    /**
     * RakeProtocolV2 의 HTTP 응답 규격을 정의
     *
     * @param responseCode 200 (OK) 일 경우만 성공적으로 서버에서 처리했음을 나타냄. (Protocol V2 에서 body 는 검증하지 않음)
     * @return
     */
    public static Status interpretResponse(int responseCode) {
        if (isValidResponseCode(responseCode)) return DONE;

        Logger.e("Server returned negative response. make sure that your token is valid");

        return interpretResponseCode(responseCode);
    }

    public static void reportResponse(String responseBody, int responseCode) {
        String message = String.format(Locale.US, "[NETWORK] Server returned code: %d, body: %s", responseCode, responseBody);
        Logger.t(message);
    }
}
