package com.rake.android.rkmetrics.network;

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

final public class HttpRequestSender {
    private static final String COMPRESS_FIELD_NAME = "compress";
    private static final String DEFAULT_COMPRESS_STRATEGY = "plain";
    private static final String DATA_FIELD_NAME = "data";
    private static final String CHAR_ENCODING = "UTF-8";

    public static final int CONNECTION_TIMEOUT = 3000;
    public static final int SOCKET_TIMEOUT = 120000;

    private HttpRequestSender() {}

    public static TransmissionResult sendRequest(String message, String url) {

        String encodedData = Base64Coder.encodeString(message);

        if (getCurrentAPILevelAsInt() >= APILevel.ICE_CREAM_SANDWICH.getLevel()) {
            return sendHttpUrlStreamRequest(url, encodedData);
        } else {
            return sendHttpClientRequest(url, encodedData);
        }
    }

    private static TransmissionResult sendHttpUrlStreamRequest(String endPoint, String encodedData) {

        URL url;
        OutputStream os = null;
        BufferedWriter writer = null;
        TransmissionResult result = TransmissionResult.FAILURE_UNRECOVERABLE;
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

            String message = String.format("Response code: %d, response body: %s", statusCode, responseBody);
            Logger.d(message);

        } catch (MalformedURLException e) {
            Logger.e("Invalid URL", e);
            result = TransmissionResult.FAILURE_RECOVERABLE;
        } catch (UnsupportedEncodingException e) {
            Logger.e("Invalid encoding", e);
            result = TransmissionResult.FAILURE_UNRECOVERABLE;
        } catch (ProtocolException e) {
            Logger.e("Invalid protocol", e);
            result = TransmissionResult.FAILURE_UNRECOVERABLE;
        } catch (IOException e) {
            Logger.e("Invalid protocol", e);
            result = TransmissionResult.FAILURE_RECOVERABLE;
        } catch (OutOfMemoryError e) {
            Logger.e("Memory insufficient", e);
            result = TransmissionResult.FAILURE_RECOVERABLE;
        } catch (Exception e) {
            Logger.e("Invalid protocol", e);
            result = TransmissionResult.FAILURE_UNRECOVERABLE;
        } finally {
            if (null != conn) conn.disconnect();

            StreamUtil.closeQuietly(writer);
            StreamUtil.closeQuietly(os);
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

    private static TransmissionResult sendHttpClientRequest(String endPoint, String requestMessage) {
        TransmissionResult result = TransmissionResult.FAILURE_UNRECOVERABLE;

        try {
            HttpEntity requestEntity = buildHttpClientRequestBody(requestMessage);
            HttpPost httppost = new HttpPost(endPoint);
            httppost.setEntity(requestEntity);
            HttpClient client = createHttpsClient();
            HttpResponse response = client.execute(httppost);

            if (null == response) {
                Logger.d("HttpResponse is null. Retry later");
                return TransmissionResult.FAILURE_RECOVERABLE;
            }

            HttpEntity responseEntity = response.getEntity();

            if (null == responseEntity) {
                Logger.d("HttpEntity is null. retry later");
                return TransmissionResult.FAILURE_RECOVERABLE;
            }

            String responseBody = StringUtil.inputStreamToString(responseEntity.getContent());
            int statusCode = response.getStatusLine().getStatusCode();

            String message = String.format("Response code: %d, Response body: %s", statusCode, responseBody);
            Logger.d(message);

            // TODO interpretResponseCode
            result = interpretResponse(responseBody);

        } catch(UnsupportedEncodingException e) {
            Logger.e("Invalid encoding", e);
            result = TransmissionResult.FAILURE_UNRECOVERABLE;
        } catch (IOException e) {
            Logger.e("Cannot post message to Rake Servers (May Retry)", e);
            result = TransmissionResult.FAILURE_RECOVERABLE;
        } catch (OutOfMemoryError e) {
            Logger.e("Cannot post message to Rake Servers, will not retry.", e);
            result = TransmissionResult.FAILURE_RECOVERABLE;
        } catch (GeneralSecurityException e) {
            Logger.e("Cannot build SSL Client", e);
        } catch (Exception e) {
            Logger.e("Uncaught exception", e);
        }

        return result;
    }

    private static TransmissionResult interpretResponseCode(int statusCode) {
        // TODO HttpsUrlConnection.HTTP_OK -> UrlConnection 과 HttpClient 의 상수가 다름 (값이 아니라 상수 이름)
        if (HttpStatus.SC_OK == statusCode) return TransmissionResult.SUCCESS;
        else if (HttpStatus.SC_INTERNAL_SERVER_ERROR == statusCode) {
            Logger.e("Internal Server Error. retry later");
            return TransmissionResult.FAILURE_RECOVERABLE; /* retry */
        }

        /* 20x (not 200), 3xx, 4xx */
        return  TransmissionResult.FAILURE_UNRECOVERABLE; /* not retry */
    }

    private static TransmissionResult interpretResponse(String response) {
        if (null == response) {
            Logger.e("Response body is empty. (Retry)");
            return TransmissionResult.FAILURE_RECOVERABLE;
        }

        if (response.startsWith("1")) return TransmissionResult.SUCCESS;

        Logger.e("Server returned negative response. make sure that your token is valid");
        return TransmissionResult.FAILURE_UNRECOVERABLE;
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
