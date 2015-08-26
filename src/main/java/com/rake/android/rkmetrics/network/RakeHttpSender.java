package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.util.Base64Coder;
import com.rake.android.rkmetrics.util.RakeLogger;
import com.rake.android.rkmetrics.util.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

final public class RakeHttpSender {
    public enum RequestResult {
        SUCCESS("SUCCESS"),
        FAILURE_RECOVERABLE("FAILURE_RECOVERABLE"),
        FAILURE_UNRECOVERABLE("FAILURE_UNRECOVERABLE");

        private String result;
        RequestResult(String result) { this.result = result; }
        @Override public String toString() { return result; }
    }

    public static final int CONNECTION_TIMEOUT = 3000;
    public static final int SOCKET_TIMEOUT = 120000;

    private RakeHttpSender() {}

    public static RequestResult sendPostRequest(String rawMessage,
                                          String baseEndpoint,
                                          String endpointPath) {

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        String encodedData = null;
        String compress = "plain";
        encodedData = Base64Coder.encodeString(rawMessage);

        nameValuePairs.add(new BasicNameValuePair("compress", compress));
        nameValuePairs.add(new BasicNameValuePair("data", encodedData));

        String url = baseEndpoint + endpointPath;
        RequestResult result = postHttpRequest(url, nameValuePairs);

        return result;
    }

    private static RequestResult postHttpRequest(String url, List<NameValuePair> nameValuePairs) {
        RequestResult result = RequestResult.FAILURE_UNRECOVERABLE;

        try {
            HttpClient client = createHttpsClient();
            HttpPost httppost = new HttpPost(url);

            UrlEncodedFormEntity requestEntity = new UrlEncodedFormEntity(nameValuePairs);
            httppost.setEntity(requestEntity);

            HttpResponse response = client.execute(httppost);

            if (null == response) {
                RakeLogger.d(LOG_TAG_PREFIX, "HttpResponse is null. Retry later");
                return RequestResult.FAILURE_RECOVERABLE;
            }

            HttpEntity entity = response.getEntity();

            if (null == entity) {
                RakeLogger.d(LOG_TAG_PREFIX, "HttpEntity is null. retry later");
                return RequestResult.FAILURE_RECOVERABLE;
            }

            String responseBody = StringUtils.inputStreamToString(entity.getContent());
            int statusCode = response.getStatusLine().getStatusCode();

            String message = String.format("response code: %d, response body: %s", statusCode, responseBody);

            if ("1\n".equals(responseBody)) {
                RakeLogger.d(LOG_TAG_PREFIX, message);
                result = RequestResult.SUCCESS;
            } else {
                RakeLogger.e(LOG_TAG_PREFIX, "server returned -1. make sure that your token is valid");
            }

//            // TODO: recover from other states (e.g 204, 404, 400, 50x...)
//            if (200 == statusCode) { result = RequestResult.SUCCESS; }
//            else if (500 == statusCode) {
//                RakeLogger.e(LOG_TAG_PREFIX, "response code 502. retry later");
//                result = RequestResult.FAILURE_RECOVERABLE; /* retry */
//            } else { /* 20x (not 200), 3xx, 4xx */
//                result = RequestResult.FAILURE_UNRECOVERABLE; /* not retry */
//            }

        } catch (IOException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "Cannot post message to Rake Servers (May Retry)", e);
            result = RequestResult.FAILURE_RECOVERABLE;
        } catch (OutOfMemoryError e) {
            RakeLogger.e(LOG_TAG_PREFIX, "Cannot post message to Rake Servers, will not retry.", e);
            result = RequestResult.FAILURE_UNRECOVERABLE;
        } catch (GeneralSecurityException e) {
            RakeLogger.e(LOG_TAG_PREFIX, "Cannot build SSL Client", e);
        } catch (Exception e) {
            RakeLogger.e(LOG_TAG_PREFIX, "caused by", e);
        }

        return result;
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
