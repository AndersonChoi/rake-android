package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.StreamUtil;
import com.rake.android.rkmetrics.util.StringUtil;
import com.rake.android.rkmetrics.util.TimeUtil;
import com.rake.android.rkmetrics.util.UnknownRakeStateException;

import org.apache.http.*;
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

import java.io.*;
import java.net.*;
import java.net.ProtocolException;
import java.security.GeneralSecurityException;

import static com.rake.android.rkmetrics.metric.model.Status.*;
import static com.rake.android.rkmetrics.network.FlushMethod.*;

final public class HttpRequestSender {
    public static final int CONNECTION_TIMEOUT = 3000;
    public static final int SOCKET_TIMEOUT = 120000;

    private HttpRequestSender() {}

    public static HttpRequestProcedure procedure = new HttpRequestProcedure() {
        @Override
        public ServerResponse execute(String url,
                                      String log,
                                      FlushMethod flushMethod) throws Exception {
            if (null == url) throw new UnknownRakeStateException("URL can't be NULL in HttpRequestProcedure.execute");
            if (null == log) throw new UnknownRakeStateException("log can't be NULL in HttpRequestProcedure.execute");
            if (null == flushMethod) throw new UnknownRakeStateException("flushMethod can't be NULL in HttpRequestProcedure.execute");

            /** 4.0 이상일 경우 HttpUrlConnection 이용 */
            if (HTTP_URL_CONNECTION == flushMethod)
                return HttpRequestSender.sendHttpUrlStreamRequest(url, log);
            else
                return HttpRequestSender.sendHttpClientRequest(url, log);
        }
    };

    public static ServerResponse handleResponse(String url,
                                                String log,
                                                FlushMethod flushMethod,
                                                HttpRequestProcedure callback) {

        Status flushStatus = DROP;
        ServerResponse responseMetric = null;

        try {
            responseMetric = callback.execute(url, log, flushMethod);
        } catch(UnsupportedEncodingException e) {
            Logger.e("Invalid encoding", e);
            return ServerResponse.createErrorResponse(e, DROP, flushMethod);
        } catch (GeneralSecurityException e) {
            Logger.e("SSL error (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP, flushMethod);
        } catch (MalformedURLException e) {
            Logger.e("Malformed url (DROP)", e);
            return ServerResponse.createErrorResponse(e, DROP, flushMethod);
        }  catch (ProtocolException e) {
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

        flushStatus = RakeProtocolV2.interpretResponse(responseMetric.getResponseCode());

        return responseMetric.setFlushStatus(flushStatus);
    }

    /**
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     * @throws ProtocolException
     * @throws IOException
     */
    public static ServerResponse sendHttpUrlStreamRequest(String endPoint,
                                                          String requestBody)
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
            conn.setRequestProperty("Content-Type","application/json");
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

            if (responseCode >= 400) br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
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

        return ServerResponse.create(responseBody, responseCode, operationTime, HTTP_URL_CONNECTION);
    }

    /**
     * @throws UnsupportedEncodingException
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static ServerResponse sendHttpClientRequest(String endPoint,
                                                       String requestBody)
            throws UnsupportedEncodingException, GeneralSecurityException, IOException {
        String responseBody = null;
        int responseCode = 0;
        long responseTime = 0L;

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
            return ServerResponse.createErrorResponse(
                    new UnknownRakeStateException("HttpEntity or HttpResponse is null"), RETRY, HTTP_CLIENT);
        }

        HttpEntity responseEntity = response.getEntity();
        responseBody = StringUtil.inputStreamToString(responseEntity.getContent());
        responseCode = response.getStatusLine().getStatusCode();

        return ServerResponse.create(responseBody, responseCode, responseTime, HTTP_CLIENT);
    }

    public static HttpClient createHttpsClient() throws GeneralSecurityException {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        HttpParams params = getDefaultHttpParams();
        ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        return new DefaultHttpClient(connectionManager, params);
    }

    public static HttpParams getDefaultHttpParams() {
        HttpParams params = new BasicHttpParams();

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
        return params;
    }
}
