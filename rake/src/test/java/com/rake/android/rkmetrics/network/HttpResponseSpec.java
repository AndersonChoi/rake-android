package com.rake.android.rkmetrics.network;

import com.rake.android.rkmetrics.metric.model.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import static org.assertj.core.api.Assertions.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class HttpResponseSpec {
    @Test
    public void test_flushStatus_by_responseCode() {

        // DONE
        assertThat(new HttpResponse(200, "ok", 0L)
                .getFlushStatus()).isEqualTo(Status.DONE);

        // RETRY
        assertThat(new HttpResponse(500, "internal server error", 0L)
                .getFlushStatus()).isEqualTo(Status.RETRY);
        assertThat(new HttpResponse(502, "bad gateway", 0L)
                .getFlushStatus()).isEqualTo(Status.RETRY);
        assertThat(new HttpResponse(503, "server unavailable", 0L)
                .getFlushStatus()).isEqualTo(Status.RETRY);

        // DROP
        assertThat(new HttpResponse(413, "request too long", 0L)
                .getFlushStatus()).isEqualTo(Status.DROP);
        assertThat(new HttpResponse(0, "unknown", 0L)
                .getFlushStatus()).isEqualTo(Status.DROP);
    }

    @Test
    public void test_flushStatus_by_exception() {

        // RETRY
        assertThat(new HttpResponse(new IOException("fake IOException"))
                .getFlushStatus()).isEqualTo(Status.RETRY);
        assertThat(new HttpResponse(new OutOfMemoryError("fake OutOfMemoryError"))
                .getFlushStatus()).isEqualTo(Status.RETRY);

        // DROP
        assertThat(new HttpResponse(new UnsupportedEncodingException("fake UnsupportedEncodingException"))
                .getFlushStatus()).isEqualTo(Status.DROP);
        assertThat(new HttpResponse(new MalformedURLException("fake MalformedURLException"))
                .getFlushStatus()).isEqualTo(Status.DROP);
        assertThat(new HttpResponse(new ProtocolException("fake ProtocolException"))
                .getFlushStatus()).isEqualTo(Status.DROP);
        assertThat(new HttpResponse(new Exception("fake Exception"))
                .getFlushStatus()).isEqualTo(Status.DROP);
        assertThat(new HttpResponse(new Throwable("fake Throwable"))
                .getFlushStatus()).isEqualTo(Status.DROP);
    }
}
