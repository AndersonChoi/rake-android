package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.util.Base64Coder;
import com.rake.android.rkmetrics.util.RakeLogger;
import com.rake.android.rkmetrics.util.StreamUtils;
import com.rake.android.rkmetrics.util.StringUtils;
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
import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

final public class HttpRequestSender {
    public enum RequestResult {
        SUCCESS("SUCCESS"),
        FAILURE_RECOVERABLE("FAILURE_RECOVERABLE"),
        FAILURE_UNRECOVERABLE("FAILURE_UNRECOVERABLE");

        private String result;
        RequestResult(String result) { this.result = result; }
        @Override public String toString() { return result; }
    }

    private static final String COMPRESS_FIELD_NAME = "compress";
    private static final String DEFAULT_COMPRESS_STRATEGY = "plain";
    private static final String DATA_FIELD_NAME = "data";
    private static final String CHAR_ENCODING = "UTF-8";

    public static final int CONNECTION_TIMEOUT = 3000;
    public static final int SOCKET_TIMEOUT = 120000;

    private HttpRequestSender() {}

    public static RequestResult sendRequest(String message, String url) {

        String encodedData = Base64Coder.encodeString(message);

        if (getCurrentAPILevelAsInt() >= APILevel.ICE_CREAM_SANDWICH.getLevel()) {
            return sendHttpUrlStreamRequest(url, encodedData);
        } else {
            return sendHttpClientRequest(url, encodedData);
        }
    }

    private static RequestResult sendHttpUrlStreamRequest(String endPoint, String encodedData) {

        URL url;
        OutputStream os = null;
        BufferedWriter writer = null;
        RequestResult result = RequestResult.FAILURE_UNRECOVERABLE;
        HttpURLConnection conn = null;
        StringBuilder builder = new StringBuilder();

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

            os = conn.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(os, CHAR_ENCODING));
            writer.write(requestBody);
            writer.flush();

            // TODO status code handling
            int statusCode = conn.getResponseCode();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = br.readLine()) != null) builder.append(line);

            String responseBody = builder.toString();
            result = interpretResponse(responseBody);

            String message = String.format("response code: %d, response body: %s", statusCode, responseBody);
            RakeLogger.d(LOG_TAG_PREFIX, message);

        } catch (MalformedURLException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "invalid URL", e);
            result = RequestResult.FAILURE_RECOVERABLE;
        } catch (UnsupportedEncodingException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "invalid encoding", e);
            result = RequestResult.FAILURE_UNRECOVERABLE;
        } catch (ProtocolException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "invalid protocol", e);
            result = RequestResult.FAILURE_UNRECOVERABLE;
        } catch (IOException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "invalid protocol", e);
            result = RequestResult.FAILURE_RECOVERABLE;
        } catch (OutOfMemoryError e) {
            RakeLogger.e(LOG_TAG_PREFIX, "memory insufficient", e);
            result = RequestResult.FAILURE_RECOVERABLE;
        } catch (Exception e) {
            RakeLogger.e(LOG_TAG_PREFIX, "invalid protocol", e);
            result = RequestResult.FAILURE_UNRECOVERABLE;
        } finally {
            if (null != conn) conn.disconnect();

            StreamUtils.closeQuietly(writer);
            StreamUtils.closeQuietly(os);
        }

        return result;
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

    private static RequestResult sendHttpClientRequest(String endPoint, String requestMessage) {
        RequestResult result = RequestResult.FAILURE_UNRECOVERABLE;

        try {
            HttpEntity requestEntity = buildHttpClientRequestBody(requestMessage);
            HttpPost httppost = new HttpPost(endPoint);
            httppost.setEntity(requestEntity);
            HttpClient client = createHttpsClient();
            HttpResponse response = client.execute(httppost);

            if (null == response) {
                RakeLogger.d(LOG_TAG_PREFIX, "HttpResponse is null. Retry later");
                return RequestResult.FAILURE_RECOVERABLE;
            }

            HttpEntity responseEntity = response.getEntity();

            if (null == responseEntity) {
                RakeLogger.d(LOG_TAG_PREFIX, "HttpEntity is null. retry later");
                return RequestResult.FAILURE_RECOVERABLE;
            }

            String responseBody = StringUtils.inputStreamToString(responseEntity.getContent());
            int statusCode = response.getStatusLine().getStatusCode();

            String message = String.format("response code: %d, response body: %s", statusCode, responseBody);
            RakeLogger.d(LOG_TAG_PREFIX, message);

            // TODO interpretResponseCode
            result = interpretResponse(responseBody);

        } catch(UnsupportedEncodingException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "invalid encoding", e);
            result = RequestResult.FAILURE_UNRECOVERABLE;
        } catch (IOException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "cannot post message to Rake Servers (May Retry)", e);
            result = RequestResult.FAILURE_RECOVERABLE;
        } catch (OutOfMemoryError e) {
            RakeLogger.e(LOG_TAG_PREFIX, "cannot post message to Rake Servers, will not retry.", e);
            result = RequestResult.FAILURE_RECOVERABLE;
        } catch (GeneralSecurityException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "cannot build SSL Client", e);
        } catch (Exception e) {
            RakeLogger.e(LOG_TAG_PREFIX, "caused by", e);
        }

        return result;
    }

    private static RequestResult interpretResponseCode(int statusCode) {
        // TODO HttpsUrlConnection.HTTP_OK -> UrlConnection 과 HttpClient 의 상수가 다름 (값이 아니라 상수 이름)
        if (HttpStatus.SC_OK == statusCode) return RequestResult.SUCCESS;
        else if (HttpStatus.SC_INTERNAL_SERVER_ERROR == statusCode) {
            RakeLogger.e(LOG_TAG_PREFIX, "Internal Server Error. retry later");
            return RequestResult.FAILURE_RECOVERABLE; /* retry */
        }

        /* 20x (not 200), 3xx, 4xx */
        return  RequestResult.FAILURE_UNRECOVERABLE; /* not retry */
    }


    private static RequestResult interpretResponse(String response) {
        if (null == response) {
            RakeLogger.e(LOG_TAG_PREFIX, "response body is empty. retry later");
            return RequestResult.FAILURE_RECOVERABLE;
        }

        if (response.startsWith("1")) return RequestResult.SUCCESS;

        RakeLogger.e(LOG_TAG_PREFIX, "server returned negative response. make sure that your token is valid");
        return RequestResult.FAILURE_UNRECOVERABLE;
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
