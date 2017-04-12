package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.StreamUtil;
import com.rake.android.rkmetrics.util.TimeUtil;
import com.rake.android.rkmetrics.util.UnknownRakeStateException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.GeneralSecurityException;

import static com.rake.android.rkmetrics.metric.model.Status.DROP;
import static com.rake.android.rkmetrics.metric.model.Status.RETRY;

final public class HttpRequestSender {
    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int SOCKET_TIMEOUT = 120000;

    private HttpRequestSender() {
    }

    public static HttpRequestProcedure procedure = new HttpRequestProcedure() {
        @Override
        public ServerResponse execute(String url, String log) throws Exception {
            if (null == url)
                throw new UnknownRakeStateException("URL can't be NULL in HttpRequestProcedure.execute");
            if (null == log)
                throw new UnknownRakeStateException("log can't be NULL in HttpRequestProcedure.execute");

            // (2017.04) Rake-Android 사용 앱들의 Froyo(Android 2.2, versionCode = 8) 이하 단말 지원 종료.
            // 따라서 apache HttpClient 사용을 제거하고 HttpURLConnection으로 network 요청 통일. (Google 권장 사항)
            return sendHttpUrlStreamRequest(url, log);
        }
    };

    public static ServerResponse handleResponse(String url, String log, HttpRequestProcedure callback) {
        ServerResponse responseMetric;

        try {
            responseMetric = callback.execute(url, log);
        } catch (UnsupportedEncodingException e) {
            Logger.e("Invalid encoding", e);
            return ServerResponse.createErrorResponse(e, DROP);
        } catch (GeneralSecurityException e) {
            Logger.e("SSL error (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP);
        } catch (MalformedURLException e) {
            Logger.e("Malformed url (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP);
        } catch (ProtocolException e) {
            Logger.e("Invalid protocol (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP);
        } catch (IOException e) {
            Logger.e("Can't post message to Rake Server (RETRY)", e);
            return ServerResponse.createErrorResponse(e, RETRY);
        } catch (OutOfMemoryError e) {
            Logger.e("Can't post message to Rake Server (RETRY)", e);
            return ServerResponse.createErrorResponse(e, RETRY);
        } catch (Exception e) {
            Logger.e("Uncaught exception (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP);
        } catch (Throwable e) {
            Logger.e("Uncaught throwable (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP);
        }

        Status flushStatus = RakeProtocolV2.interpretResponse(responseMetric.getResponseCode());

        return responseMetric.setFlushStatus(flushStatus);
    }

    /**
     * @throws IOException (including MalformedURLException, UnsupportedEncodingException, ProtocolException)
     */
    private static ServerResponse sendHttpUrlStreamRequest(String endPoint, String requestBody) throws IOException {

        URL url;
        OutputStream os = null;
        BufferedWriter writer = null;
        BufferedReader br = null;
        HttpURLConnection conn = null;
        StringBuilder builder = new StringBuilder();

        int responseCode = 0;
        String responseBody = null;
        long operationTime = 0L;

        try {
            url = new URL(endPoint);
            conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(SOCKET_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("Accept-Encoding", "identity"); /* disable default gzip */
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            long startAt = System.nanoTime();

            os = conn.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(os, RakeProtocolV2.CHAR_ENCODING));
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
        }

        return ServerResponse.create(responseBody, responseCode, operationTime);
    }
}
