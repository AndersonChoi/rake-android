package com.rake.android.rkmetrics;

import android.app.Application;
import android.os.Handler;

import com.rake.android.rkmetrics.network.Endpoint;
import com.rake.android.rkmetrics.util.functional.Callback;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.rake.android.rkmetrics.network.Endpoint.*;
import com.rake.android.rkmetrics.RakeAPI.Env;
import com.rake.android.rkmetrics.RakeAPI.Logging;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class RakeAPISpec {

    public static String genToken() {
        return UUID.randomUUID().toString();
    }

    Application app = RuntimeEnvironment.application;
    RakeAPI rake = RakeAPI.getInstance(
            app,
            "exampleToken",
            RakeAPI.Env.DEV,
            RakeAPI.Logging.ENABLE
            );

    @Test
    public void setFlushInterval() {
        long defaultInterval = MessageLoop.DEFAULT_FLUSH_INTERVAL;
        long newInterval = 5 * 1000L;

        assertEquals(MessageLoop.DEFAULT_FLUSH_INTERVAL,
                RakeAPI.getFlushInterval());

        RakeAPI.setFlushInterval(newInterval);

        assertEquals(newInterval,
                RakeAPI.getFlushInterval());
    }

    @Test
    public void test_Env_Live_시_라이브서버_과금_URL_로_세팅되어야함() {
        RakeAPI r = RakeAPI.getInstance(app, genToken(), Env.DEV, Logging.ENABLE);
        assertEquals(DEV_ENDPOINT_CHARGED, r.getEndpoint());
    }

    @Test
    public void test_Env_Dev_시_개발서버_과금_URL_로_세팅되어야함() {
        RakeAPI r = RakeAPI.getInstance(app, genToken(), Env.LIVE, Logging.ENABLE);
        assertEquals(Endpoint.LIVE_ENDPOINT_CHARGED, r.getEndpoint());
    }

    @Test
    public void 비과금에서_무과금으로_URL_변경이가능해야함() {
        RakeAPI devRake = RakeAPI.getInstance(app, genToken(), Env.DEV, Logging.ENABLE);
        devRake.setEndpoint(DEV_ENDPOINT_FREE);
        assertEquals(DEV_ENDPOINT_FREE, devRake.getEndpoint());

        RakeAPI liveRake = RakeAPI.getInstance(app, genToken(), Env.LIVE, Logging.ENABLE);
        liveRake.setEndpoint(LIVE_ENDPOINT_FREE);
        assertEquals(LIVE_ENDPOINT_FREE, liveRake.getEndpoint());
    }

    @Test
    public void 개발과_라이브간_URL_전환시_IllegalArgumentException_을_던져야함() {

        List<Endpoint> devEndpoints =
                Arrays.asList(DEV_ENDPOINT_CHARGED, DEV_ENDPOINT_FREE);

        List<Endpoint> liveEndpoints =
                Arrays.asList(LIVE_ENDPOINT_CHARGED, LIVE_ENDPOINT_FREE);

        // 개발 과금 -> 라이브 과금
        // 개발 과금 -> 라이브 무과금
        // 개발 무과금 -> 라이브 과금
        // 개발 무과금 -> 라이브 무과금
        for(final Endpoint dev : devEndpoints) {
            for(final Endpoint live : liveEndpoints) {
                handledIllegalArgumentException(new Callback() {
                    @Override
                    public void execute() {
                        RakeAPI r1 = RakeAPI.getInstance(app, genToken(), Env.DEV, Logging.ENABLE);
                        r1.setEndpoint(dev);

                        r1.setEndpoint(live); /* IllegalArgumentException occurred */
                    }
                });
            }
        }

        // 라이브 과금 -> 개발 과금
        // 라이브 과금 -> 개발 무과금
        // 라이브 무과금 -> 개발 과금
        // 라이브 무과금 -> 개발 과금
        for(final Endpoint live : liveEndpoints) {
            for(final Endpoint dev : devEndpoints) {
                handledIllegalArgumentException(new Callback() {
                    @Override
                    public void execute() {
                        RakeAPI r1 = RakeAPI.getInstance(app, genToken(), Env.LIVE, Logging.ENABLE);
                        r1.setEndpoint(live);

                        r1.setEndpoint(dev); /* IllegalArgumentException occurred */
                    }
                });
            }
        }
    }

    public static void handledIllegalArgumentException(Callback callback) {
        try { callback.execute(); throw new RuntimeException("Expected IllegalArgumentException"); }
        catch (IllegalArgumentException e) { /* ignore */ }
    }

    @Test
    public void test_복수의_Env_를_지원해야함() {
        RakeAPI r1 = RakeAPI.getInstance(app, genToken(), Env.DEV, Logging.ENABLE);
        assertEquals(Endpoint.DEV_ENDPOINT_CHARGED, r1.getEndpoint());

        RakeAPI r2 = RakeAPI.getInstance(app, genToken(), Env.LIVE, Logging.ENABLE);
        assertEquals(Endpoint.LIVE_ENDPOINT_CHARGED, r2.getEndpoint());
    }
}
