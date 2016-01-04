package com.rake.android.rkmetrics.network;

import static com.rake.android.rkmetrics.metric.model.Status.*;
import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.util.Base64Coder;
import com.rake.android.rkmetrics.util.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RakeProtocolV1 {
    public static final String COMPRESS_FIELD_NAME = "compress";
    public static final String DEFAULT_COMPRESS_STRATEGY = "plain";
    public static final String DATA_FIELD_NAME = "data";

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

    /**
     * RakeProtocolV1 의 유효한 HTTP Response Body 규격을 정의
     * @param body
     * @return true iff response body starts with "1" otherwise return false
     */
    public static boolean isValidResponseBody(String body) {
        if (null == body) return false;
        if (body.startsWith("1")) return true;

        return false;
    }

    public static boolean isValidResponseCode(int code) {
        /**
         * HttpURLConnection.HTTP_OK 와 HttpStatus.SC_OK 둘다 200 이므로 값으로 200 을 사용
         */

        return code == HTTP_STATUS_CODE_OK;
    }

    /**
     * RakeProtocolV1 의 HTTP 응답 규격을 정의
     *
     * @param responseBody "1" 로 시작할 경우만 성공적으로 서버에서 처리했음을 나타냄
     * @param responseCode 200 (OK) 일 경우만 성공적으로 서버에서 처리했음을 나타냄
     * @return
     */
    public static Status interpretResponse(String responseBody, int responseCode) {
        if (isValidResponseBody(responseBody) && isValidResponseCode(responseCode)) return DONE;

        Logger.e("Server returned negative response. make sure that your token is valid");

        return interpretResponseCode(responseCode);
    }

    public static void reportResponse(String responseBody, int responseCode) {
        String message = String.format("[NETWORK] Server returned code: %d, body: %s", responseCode, responseBody);
        Logger.t(message);
    }

    public static String buildHttpUrlConnectionRequestBody(String message)
            throws UnsupportedEncodingException {

        Map<String, String> params = new HashMap<String, String>();

        String base64Encoded = Base64Coder.encodeString(message);
        params.put(COMPRESS_FIELD_NAME, DEFAULT_COMPRESS_STRATEGY);
        params.put(DATA_FIELD_NAME, base64Encoded);

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for(Map.Entry<String, String> entry : params.entrySet()) {
            if (first) first = false;
            else result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), HttpRequestSender.CHAR_ENCODING));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), HttpRequestSender.CHAR_ENCODING));
        }

        return result.toString();
    }

    public static HttpEntity buildHttpClientRequestBody(String message)
            throws UnsupportedEncodingException {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

        String base64Encoded = Base64Coder.encodeString(message);
        nameValuePairs.add(new BasicNameValuePair(COMPRESS_FIELD_NAME, DEFAULT_COMPRESS_STRATEGY));
        nameValuePairs.add(new BasicNameValuePair(DATA_FIELD_NAME, base64Encoded));

        return new UrlEncodedFormEntity(nameValuePairs);
    }
}
