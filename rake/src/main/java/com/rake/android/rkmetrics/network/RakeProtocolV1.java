package com.rake.android.rkmetrics.network;

import static com.rake.android.rkmetrics.metric.model.Status.*;
import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.util.Logger;

import org.apache.http.HttpStatus;

public final class RakeProtocolV1 {

    public static Status interpretResponseCode(int statusCode) {
        // TODO HttpsUrlConnection.HTTP_OK -> UrlConnection 과 HttpClient 의 상수가 다름 (값이 아니라 상수 이름)
        if (HttpStatus.SC_OK == statusCode) return DONE;
        else if (HttpStatus.SC_INTERNAL_SERVER_ERROR == statusCode) {
            Logger.e("Internal Server Error. retry later");
            return RETRY;
        }

        /* 20x (not 200), 3xx, 4xx */
        return DROP; /* not retry */
    }

    /**
     * RakeProtocolV1 의 HTTP 응답 규격을 정의
     * @param responseBody
     * @param responseCode 사용하지 않음
     * @return
     */
    public static Status interpretResponse(String responseBody, int responseCode) {
        if (null == responseBody) {
            return RETRY;
        }

        if (responseBody.startsWith("1")) return DONE;

        Logger.e("Server returned negative response. make sure that your token is valid");

        return DROP;
    }


    public static void reportResponse(String responseBody, int responseCode) {
        String message = String.format("Server returned code: %d, body: %s", responseCode, responseBody);
        Logger.d(message);
    }
}
