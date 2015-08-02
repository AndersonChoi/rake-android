package com.rake.android.rkmetrics.network;

import static com.rake.android.rkmetrics.config.RakeConfig.LOG_TAG_PREFIX;

import com.rake.android.rkmetrics.config.RakeConfig;
import com.rake.android.rkmetrics.util.Base64Coder;
import com.rake.android.rkmetrics.util.RakeLogger;
import com.rake.android.rkmetrics.util.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

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

    private static HttpParams getDefaultHttpParams() {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);
        return httpParameters;
    }

    private static RequestResult postHttpRequest(String url, List<NameValuePair> nameValuePairs) {
        RequestResult result = RequestResult.FAILURE_UNRECOVERABLE;

        HttpParams params = getDefaultHttpParams();
        HttpClient httpclient = new DefaultHttpClient(params);
        HttpPost httppost = new HttpPost(url);

        try {
            if (url.indexOf("https") >= 0 && RakeConfig.USE_HTTPS) {
                httpclient = createSSLClient(httpclient);
            }

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);

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
            RakeLogger.d(LOG_TAG_PREFIX, message);

            if ("1\n".equals(responseBody)) {
                result = RequestResult.SUCCESS;
            } else {
                RakeLogger.e(LOG_TAG_PREFIX, "server returned -1. make sure that your token is valid");
            }

            // TODO: recover from other states (e.g 204, 404, 400, 50x...)
//                if (200 == statusCode) { ret = RequestResult.SUCCESS; }
//                else if (500 == statusCode) { ret = RequestResult.FAILURE_RECOVERABLE; /* retry */ }
//                else { ret = RequestResult.FAILURE_UNRECOVERABLE; /* not retry */ }


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

    private static HttpClient createSSLClient(HttpClient client) throws GeneralSecurityException {
        X509TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory ssf = new RakeSSLSocketFactory(ctx);
        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        ClientConnectionManager ccm = client.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", ssf, 443));
        return new DefaultHttpClient(ccm, client.getParams());
    }

    private static class RakeSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public RakeSSLSocketFactory(KeyStore store) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(store);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        public RakeSSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
            super(null);
            sslContext = context;
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
