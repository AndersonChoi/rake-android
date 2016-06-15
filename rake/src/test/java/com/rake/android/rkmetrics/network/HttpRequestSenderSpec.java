package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.util.UnknownRakeStateException;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;

import static com.rake.android.rkmetrics.metric.model.Status.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class HttpRequestSenderSpec {

    /** DROP cases */

    public void assertFlushStatusReturnedFromHandleResponse(Throwable e, Status status) {
        ServerResponse metric = HttpRequestSender.handleResponse(
                null, null, FlushMethod.HTTP_URL_CONNECTION, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(status);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_UnsupportedEncodingException() {
        Throwable e= new UnsupportedEncodingException("");
        assertFlushStatusReturnedFromHandleResponse(e, DROP);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_GeneralSecurityException() {
        Throwable e= new GeneralSecurityException("");
        assertFlushStatusReturnedFromHandleResponse(e, DROP);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_MalformedURLException() {
        Throwable e= new MalformedURLException("");
        assertFlushStatusReturnedFromHandleResponse(e, DROP);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_ProtocolException() {
        Throwable e= new ProtocolException("");
        assertFlushStatusReturnedFromHandleResponse(e, DROP);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_UnknownRakeStateException() {
        Throwable e= new UnknownRakeStateException("");
        assertFlushStatusReturnedFromHandleResponse(e, DROP);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_Exception() {
        Throwable e= new Exception("");
        assertFlushStatusReturnedFromHandleResponse(e, DROP);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_Throwable() {
        Throwable e= new Throwable("");
        assertFlushStatusReturnedFromHandleResponse(e, DROP);
    }

    /** RETRY cases */

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_IOException() {
        Throwable e= new IOException("");
        assertFlushStatusReturnedFromHandleResponse(e, RETRY);
    }

    public void handleException_should_mark_RETRY_given_procedure_throw_UnknownHostException() {
        Throwable e= new UnknownHostException("");
        assertFlushStatusReturnedFromHandleResponse(e, RETRY);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_SocketTimeoutException() {
        Throwable e= new SocketTimeoutException("");
        assertFlushStatusReturnedFromHandleResponse(e, RETRY);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_SSLException() {
        Throwable e= new SSLException("");
        assertFlushStatusReturnedFromHandleResponse(e, RETRY);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_SSLHandshakeException() {
        Throwable e= new SSLHandshakeException("");
        assertFlushStatusReturnedFromHandleResponse(e, RETRY);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_SSLProtocolException() {
        Throwable e= new SSLProtocolException("");
        assertFlushStatusReturnedFromHandleResponse(e, RETRY);
    }


    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_BindException() {
        Throwable e= new BindException("");
        assertFlushStatusReturnedFromHandleResponse(e, RETRY);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_ConnectException() {
        Throwable e= new ConnectException("");
        assertFlushStatusReturnedFromHandleResponse(e, RETRY);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_SocketException() {
        Throwable e= new SocketException("");
        assertFlushStatusReturnedFromHandleResponse(e, RETRY);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_OutOfMemory() {
        Throwable e = new OutOfMemoryError("");
        assertFlushStatusReturnedFromHandleResponse(e, RETRY);
    }

    @Test(expected = UnknownRakeStateException.class)
    public void procedure_should_throw_UnknownRakeStateException_given_url_is_NULL() throws Throwable {
        HttpRequestSender.procedure.execute(null, "log", FlushMethod.HTTP_URL_CONNECTION);
    }

    @Test(expected = UnknownRakeStateException.class)
    public void procedure_should_throw_UnknownRakeStateException_given_log_is_NULL() throws Throwable {
        HttpRequestSender.procedure.execute("url", null, FlushMethod.HTTP_URL_CONNECTION);
    }

    @Test(expected = UnknownRakeStateException.class)
    public void procedure_should_throw_UnknownRakeStateException_given_flushMethod_is_NULL() throws Throwable {
        HttpRequestSender.procedure.execute("url", "log", null);
    }

    public HttpRequestProcedure createProcedureThrowingException(final Throwable e) {
        return new HttpRequestProcedure() {
            @Override
            public ServerResponse execute(String url, String log, FlushMethod flushMethod) throws Throwable {
                throw e;
            }
        };
    }
}
