package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.FlushResult;
import com.rake.android.rkmetrics.util.Base64Coder;
import com.rake.android.rkmetrics.util.Logger;
import com.rake.android.rkmetrics.util.StreamUtil;
import com.rake.android.rkmetrics.util.StringUtil;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.*;
import java.net.*;
import java.net.ProtocolException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rake.android.rkmetrics.android.Compatibility.*;
import static com.rake.android.rkmetrics.metric.model.FlushResult.*;

final public class HttpRequestSender {
    private static final String COMPRESS_FIELD_NAME = "compress";
    private static final String DEFAULT_COMPRESS_STRATEGY = "plain";
    private static final String DATA_FIELD_NAME = "data";
    private static final String CHAR_ENCODING = "UTF-8";

    public static final int CONNECTION_TIMEOUT = 3000;
    public static final int SOCKET_TIMEOUT = 120000;

    private HttpRequestSender() {}

    public static ServerResponseMetric sendRequest(String message, String url) {

        String encodedData = Base64Coder.encodeString(message);

        if (getCurrentAPILevelAsInt() >= APILevel.ICE_CREAM_SANDWICH.getLevel()) {
            return sendHttpUrlStreamRequest(url, encodedData);
        } else {
            return sendHttpClientRequest(url, encodedData);
        }
    }

    private static ServerResponseMetric sendHttpUrlStreamRequest(String endPoint, String encodedData) {

        URL url;
        OutputStream os = null;
        BufferedWriter writer = null;
        HttpURLConnection conn = null;
        StringBuilder builder = new StringBuilder();

        int responseCode = 0;
        String responseBody = null;
        FlushResult flushResult = FAILURE_UNRECOVERABLE;
        long operationTime = 0L;
        Throwable t = null;

        try {
            url = new URL(endPoint);
            conn = (HttpURLConnection) url.openConnection();
            String requestBody = buildHttpUrlConnectionRequestBody(encodedData);

            conn.setReadTimeout(SOCKET_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("Accept-Encoding", "identity"); /* disable default gzip */
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            long startAt = System.currentTimeMillis();

            os = conn.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(os, CHAR_ENCODING));
            writer.write(requestBody);
            writer.flush();

            long endAt = System.currentTimeMillis();
            operationTime = (endAt - startAt);

            // TODO status code handling
            responseCode = conn.getResponseCode();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = br.readLine()) != null) builder.append(line);

            responseBody = builder.toString();
            flushResult = interpretResponse(responseBody);

            reportResponse(responseCode, responseBody);
        } catch (MalformedURLException e) {
            Logger.e("Invalid URL", e);
            flushResult = FAILURE_RECOVERABLE;
            t = e;
        } catch (UnsupportedEncodingException e) {
            Logger.e("Invalid encoding", e);
            flushResult = FAILURE_UNRECOVERABLE;
            t = e;
        } catch (ProtocolException e) {
            Logger.e("Invalid protocol", e);
            flushResult = FAILURE_UNRECOVERABLE;
            t = e;
        } catch (IOException e) {
            Logger.e("Invalid protocol", e);
            flushResult = FAILURE_RECOVERABLE;
            t = e;
        } catch (OutOfMemoryError e) {
            Logger.e("Memory insufficient", e);
            flushResult = FAILURE_RECOVERABLE;
            t = e;
        } catch (Exception e) {
            Logger.e("Invalid protocol", e);
            flushResult = FAILURE_UNRECOVERABLE;
            t = e;
        } finally {
            if (null != conn) conn.disconnect();

            StreamUtil.closeQuietly(writer);
            StreamUtil.closeQuietly(os);
        }

        return ServerResponseMetric.create(t, flushResult, responseBody, responseCode, operationTime);
    }

    private static void reportResponse(int responseCode, String responseBody) {
        String message = String.format("Server returned code: %d, body: %s", responseCode, responseBody);
        Logger.d(message);
    }

    private static String buildHttpUrlConnectionRequestBody(String encodedDate) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<String, String>();

        params.put(COMPRESS_FIELD_NAME, DEFAULT_COMPRESS_STRATEGY);
        params.put(DATA_FIELD_NAME, encodedDate);

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for(Map.Entry<String, String> entry : params.entrySet()) {
            if (first) first = false;
            else result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), CHAR_ENCODING));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), CHAR_ENCODING));
        }

        return result.toString();
    }

    private static HttpEntity buildHttpClientRequestBody(String encodedData) throws UnsupportedEncodingException {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

        nameValuePairs.add(new BasicNameValuePair(COMPRESS_FIELD_NAME, DEFAULT_COMPRESS_STRATEGY));
        nameValuePairs.add(new BasicNameValuePair(DATA_FIELD_NAME, encodedData));

        return new UrlEncodedFormEntity(nameValuePairs);
    }

    private static ServerResponseMetric sendHttpClientRequest(String endPoint, String requestMessage) {
        FlushResult flushResult = FAILURE_UNRECOVERABLE;
        String responseBody = null;
        int responseCode = 0;
        long operationTime = 0L;
        Throwable t = null;

        try {
            HttpEntity requestEntity = buildHttpClientRequestBody(requestMessage);
            HttpPost httppost = new HttpPost(endPoint);
            httppost.setEntity(requestEntity);
            HttpClient client = createHttpsClient();

            long startAt = System.currentTimeMillis();
            HttpResponse response = client.execute(httppost);
            long endAt = System.currentTimeMillis();
            operationTime = (endAt - startAt);

            if (null == response) {
                Logger.d("HttpResponse is null. Retry later");
                return ServerResponseMetric.create(null, FAILURE_RECOVERABLE, null, 0, operationTime);
            }

            HttpEntity responseEntity = response.getEntity();

            if (null == responseEntity) {
                Logger.d("HttpEntity is null. Retry later");
                return ServerResponseMetric.create(null, FAILURE_RECOVERABLE, null, 0, operationTime);
            }

            responseBody = StringUtil.inputStreamToString(responseEntity.getContent());
            responseCode = response.getStatusLine().getStatusCode();

            // TODO interpretResponseCode
            flushResult = interpretResponse(responseBody);

            reportResponse(responseCode, responseBody);
        } catch(UnsupportedEncodingException e) {
            Logger.e("Invalid encoding", e);
            flushResult = FAILURE_UNRECOVERABLE;
            t = e;
        } catch (IOException e) {
            Logger.e("Cannot post message to Rake Servers (May Retry)", e);
            flushResult = FAILURE_RECOVERABLE;
            t = e;
        } catch (OutOfMemoryError e) {
            Logger.e("Cannot post message to Rake Servers, will not retry.", e);
            flushResult = FAILURE_RECOVERABLE;
            t = e;
        } catch (GeneralSecurityException e) {
            Logger.e("Cannot build SSL Client", e);
            t = e;
        } catch (Exception e) {
            Logger.e("Uncaught exception", e);
            t = e;
        }

        return ServerResponseMetric.create(
                t, flushResult, responseBody, responseCode, operationTime);
    }

    private static FlushResult interpretResponseCode(int statusCode) {
        // TODO HttpsUrlConnection.HTTP_OK -> UrlConnection 과 HttpClient 의 상수가 다름 (값이 아니라 상수 이름)
        if (HttpStatus.SC_OK == statusCode) return SUCCESS;
        else if (HttpStatus.SC_INTERNAL_SERVER_ERROR == statusCode) {
            Logger.e("Internal Server Error. retry later");
            return FAILURE_RECOVERABLE; /* retry */
        }

        /* 20x (not 200), 3xx, 4xx */
        return  FAILURE_UNRECOVERABLE; /* not retry */
    }

    private static FlushResult interpretResponse(String response) {
        if (null == response) {
            Logger.e("ServerResponse body is empty. (Retry)");
            return FAILURE_RECOVERABLE;
        }

        if (response.startsWith("1")) return SUCCESS;

        Logger.e("Server returned negative response. make sure that your token is valid");
        return FAILURE_UNRECOVERABLE;
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
