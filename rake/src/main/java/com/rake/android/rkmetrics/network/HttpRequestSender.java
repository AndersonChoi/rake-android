package com.rake.android.rkmetrics.network;

import android.os.Build;

import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.StreamUtil;
import com.rake.android.rkmetrics.util.StringUtil;
import com.rake.android.rkmetrics.util.TimeUtil;
import com.rake.android.rkmetrics.util.UnknownRakeStateException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

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

    // TODO Froyo 단말 탑재가 중단되면 FLUSH_METHOD 속성값 없앨것 (HTTP_URL_CONNECTION으로 통일)
    static final String FLUSH_METHOD_HTTP_URL_CONNECTION = "HTTP_URL_CONNECTION";
    static final String FLUSH_METHOD_HTTP_CLIENT = "HTTP_CLIENT";

    private HttpRequestSender() {
    }

    public static HttpRequestProcedure procedure = new HttpRequestProcedure() {
        @Override
        public ServerResponse execute(String url, String log, String flushMethod) throws Exception {
            if (null == url)
                throw new UnknownRakeStateException("URL can't be NULL in HttpRequestProcedure.execute");
            if (null == log)
                throw new UnknownRakeStateException("log can't be NULL in HttpRequestProcedure.execute");
            if (null == flushMethod)
                throw new UnknownRakeStateException("flushMethod can't be NULL in HttpRequestProcedure.execute");

            /* 2.3 (Ginger Bread) 이상일 경우 HttpUrlConnection 이용 (Google 권장사항)*/
            if (getProperFlushMethod().equals(FLUSH_METHOD_HTTP_URL_CONNECTION)) {
                return HttpRequestSender.sendHttpUrlStreamRequest(url, log);
            }
            return HttpRequestSender.sendHttpClientRequest(url, log);
        }
    };

    public static String getProperFlushMethod() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return FLUSH_METHOD_HTTP_URL_CONNECTION;
        }
        return FLUSH_METHOD_HTTP_CLIENT;
    }

    public static ServerResponse handleResponse(String url, String log, String flushMethod, HttpRequestProcedure callback) {
        ServerResponse responseMetric;

        try {
            responseMetric = callback.execute(url, log, flushMethod);
        } catch (UnsupportedEncodingException e) {
            Logger.e("Invalid encoding", e);
            return ServerResponse.createErrorResponse(e, DROP, flushMethod);
        } catch (GeneralSecurityException e) {
            Logger.e("SSL error (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP, flushMethod);
        } catch (MalformedURLException e) {
            Logger.e("Malformed url (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP, flushMethod);
        } catch (ProtocolException e) {
            Logger.e("Invalid protocol (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP, flushMethod);
        } catch (IOException e) {
            Logger.e("Can't post message to Rake Server (RETRY)", e);
            return ServerResponse.createErrorResponse(e, RETRY, flushMethod);
        } catch (OutOfMemoryError e) {
            Logger.e("Can't post message to Rake Server (RETRY)", e);
            return ServerResponse.createErrorResponse(e, RETRY, flushMethod);
        } catch (Exception e) {
            Logger.e("Uncaught exception (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP, flushMethod);
        } catch (Throwable e) {
            Logger.e("Uncaught throwable (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP, flushMethod);
        }

        Status flushStatus = RakeProtocolV2.interpretResponse(responseMetric.getResponseCode());

        return responseMetric.setFlushStatus(flushStatus);
    }

    /**
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     * @throws ProtocolException
     * @throws IOException
     */
    private static ServerResponse sendHttpUrlStreamRequest(String endPoint, String requestBody)
            throws MalformedURLException, UnsupportedEncodingException, ProtocolException, IOException {

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

            if (responseCode >= 400)
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            else br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;

            while ((line = br.readLine()) != null) builder.append(line);

            responseBody = builder.toString();

        } finally {
            StreamUtil.closeQuietly(br);
            StreamUtil.closeQuietly(writer);
            StreamUtil.closeQuietly(os);

            if (null != conn) conn.disconnect();
        }

        return ServerResponse.create(responseBody, responseCode, operationTime, FLUSH_METHOD_HTTP_URL_CONNECTION);
    }

    /**
     * @throws UnsupportedEncodingException
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private static ServerResponse sendHttpClientRequest(String endPoint, String requestBody)
            throws UnsupportedEncodingException, GeneralSecurityException, IOException {
        String responseBody;
        int responseCode;
        long responseTime;

        StringEntity requestEntity = new StringEntity(requestBody);
        HttpPost httppost = new HttpPost(endPoint);
        httppost.setHeader("Content-type", "application/json");
        httppost.setEntity(requestEntity);
        HttpClient client = createHttpsClient();

        long startAt = System.currentTimeMillis();
        HttpResponse response = client.execute(httppost);
        long endAt = System.currentTimeMillis();
        responseTime = TimeUtil.convertNanoTimeDurationToMillis(startAt, endAt);

        if (null == response || null == response.getEntity()) {
            Logger.d("HttpResponse or HttpEntity is null. Retry later");
            return ServerResponse.createErrorResponse(new UnknownRakeStateException("HttpEntity or HttpResponse is null"), RETRY, FLUSH_METHOD_HTTP_CLIENT);
        }

        HttpEntity responseEntity = response.getEntity();
        responseBody = StringUtil.inputStreamToString(responseEntity.getContent());
        responseCode = response.getStatusLine().getStatusCode();

        return ServerResponse.create(responseBody, responseCode, responseTime, FLUSH_METHOD_HTTP_CLIENT);
    }

    private static HttpClient createHttpsClient() throws GeneralSecurityException {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        HttpParams params = getDefaultHttpParams();
        ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        return new DefaultHttpClient(connectionManager, params);
    }

    private static HttpParams getDefaultHttpParams() {
        HttpParams params = new BasicHttpParams();

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
        return params;
    }
}
