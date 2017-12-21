package com.rake.android.rkmetrics.network;

import android.net.TrafficStats;
import android.os.Build;
import android.os.Process;

import com.rake.android.rkmetrics.util.StreamUtil;
import com.rake.android.rkmetrics.util.TimeUtil;
import com.rake.android.rkmetrics.util.UnknownRakeStateException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

final public class HttpRequestSender {
    public static final String RAKE_PROTOCOL_VERSION = "V2";
    private static final String CHAR_ENCODING = "UTF-8";

    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int SOCKET_TIMEOUT = 120000;

    private HttpRequestSender() {
    }

    public static HttpRequestProcedure procedure = new HttpRequestProcedure() {
        @Override
        public HttpResponse execute(String url, String log) throws Exception {
            if (null == url)
                throw new UnknownRakeStateException("URL can't be NULL in HttpRequestProcedure.execute");
            if (null == log)
                throw new UnknownRakeStateException("log can't be NULL in HttpRequestProcedure.execute");

            // (2017.04) Rake-Android 사용 앱들의 Froyo(Android 2.2, versionCode = 8) 이하 단말 지원 종료.
            // 따라서 apache HttpClient 사용을 제거하고 HttpURLConnection으로 network 요청 통일. (Google 권장 사항)
            return sendHttpUrlStreamRequest(url, log);
        }
    };

    public static HttpResponse handleResponse(String url, String log, HttpRequestProcedure callback) {
        try {
            return callback.execute(url, log);
        } catch (Exception e) {
            return new HttpResponse(e);
        } catch (Throwable e) {
            return new HttpResponse(e);
        }
    }

    /**
     * @throws IOException (including MalformedURLException, UnsupportedEncodingException, ProtocolException)
     */
    private static HttpResponse sendHttpUrlStreamRequest(String endPoint, String requestBody) throws IOException {

        URL url;
        OutputStream os = null;
        BufferedWriter writer = null;
        BufferedReader br = null;
        HttpsURLConnection conn = null;
        StringBuilder builder = new StringBuilder();

        int responseCode;
        String responseBody;
        long operationTime;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // 사용자가 어플리케이션에서 StrictMode를 사용하여 개발을 할 경우, VM Policy 위반 회피를 위해 Thread Stats Tag 추가
            TrafficStats.setThreadStatsTag(Process.myTid());
        }

        try {
            url = new URL(endPoint);
            conn = (HttpsURLConnection) url.openConnection();

            conn.setReadTimeout(SOCKET_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("Accept-Encoding", "identity"); /* disable default gzip */
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());

            long startAt = System.nanoTime();

            os = conn.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(os, CHAR_ENCODING));
            writer.write(requestBody);
            writer.flush();

            long endAt = System.nanoTime();
            operationTime = TimeUtil.convertNanoTimeDurationToMillis(startAt, endAt);

            responseCode = conn.getResponseCode();

            if (responseCode >= 400) {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }

            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }

            responseBody = builder.toString();

        } finally {
            StreamUtil.closeQuietly(br);
            StreamUtil.closeQuietly(writer);
            StreamUtil.closeQuietly(os);

            if (null != conn) {
                conn.disconnect();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                TrafficStats.clearThreadStatsTag();
            }
        }

        return new HttpResponse(responseCode, responseBody, operationTime);
    }
}
