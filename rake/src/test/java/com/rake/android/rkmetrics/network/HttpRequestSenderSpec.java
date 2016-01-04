package com.rake.android.rkmetrics.network;

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

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_UnsupportedEncodingException() {
        Throwable e= new UnsupportedEncodingException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(DROP);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_GeneralSecurityException() {
        Throwable e= new GeneralSecurityException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(DROP);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_MalformedURLException() {
        Throwable e= new MalformedURLException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(DROP);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_ProtocolException() {
        Throwable e= new ProtocolException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(DROP);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_UnknownRakeStateException() {
        Throwable e= new UnknownRakeStateException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(DROP);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_Exception() {
        Throwable e= new Exception("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(DROP);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_DROP_given_procedure_throw_Throwable() {
        Throwable e= new Throwable("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(DROP);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    /** RETRY cases */

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_IOException() {
        Throwable e= new IOException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(RETRY);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    public void handleException_should_mark_RETRY_given_procedure_throw_UnknownHostException() {
        Throwable e= new UnknownHostException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(RETRY);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_SocketTimeoutException() {
        Throwable e= new SocketTimeoutException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(RETRY);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_SSLException() {
        Throwable e= new SSLException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(RETRY);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_SSLHandshakeException() {
        Throwable e= new SSLHandshakeException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(RETRY);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_SSLProtocolException() {
        Throwable e= new SSLProtocolException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(RETRY);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }


    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_BindException() {
        Throwable e= new BindException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(RETRY);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_ConnectException() {
        Throwable e= new ConnectException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(RETRY);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_SocketException() {
        Throwable e= new SocketException("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(RETRY);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test
    public void handleException_should_mark_RETRY_given_procedure_throw_OutOfMemory() {
        Throwable e = new OutOfMemoryError("");
        ServerResponseMetric metric = HttpRequestSender.handleException(null, null, createProcedureThrowingException(e));

        assertThat(metric.getFlushStatus()).isEqualTo(RETRY);
        assertThat(metric.getExceptionInfo()).isEqualTo(e);
    }

    @Test(expected = UnknownRakeStateException.class)
    public void procedure_should_throw_UnknownRakeStateException_given_url_is_NULL() throws Throwable {
        HttpRequestSender.procedure.execute(null, "log");
    }

    @Test(expected = UnknownRakeStateException.class)
    public void procedure_should_throw_UnknownRakeStateException_given_log_is_NULL() throws Throwable {
        HttpRequestSender.procedure.execute("url", null);
    }

    public HttpRequestProcedure createProcedureThrowingException(final Throwable e) {
        return new HttpRequestProcedure() {
            @Override
            public ServerResponseMetric execute(String url, String log) throws Throwable {
                throw e;
            }
        };
    }
}
