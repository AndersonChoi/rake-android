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
        long newInterval = defaultInterval + 1000;

        assertEquals(MessageLoop.DEFAULT_FLUSH_INTERVAL,
                RakeAPI.getFlushInterval());

        RakeAPI.setFlushInterval(newInterval);

        assertEquals(newInterval,
                RakeAPI.getFlushInterval());
    }

    @Test
    public void Env_Live_시_라이브서버_과금_URL_로_세팅되어야함() {
        Env e = Env.DEV;
        RakeAPI r = RakeAPI.getInstance(app, genToken(), e, Logging.ENABLE);
        assertEquals(Endpoint.CHARGED_ENDPOINT_DEV, r.getEndpoint().getURI(e));
    }

    @Test
    public void Env_Dev_시_개발서버_과금_URL_로_세팅되어야함() {
        Env e = Env.LIVE;
        RakeAPI r = RakeAPI.getInstance(app, genToken(), e, Logging.ENABLE);
        assertEquals(Endpoint.CHARGED_ENDPOINT_LIVE, r.getEndpoint().getURI(e));
    }

    @Test
    public void 복수의_Env_를_지원해야함() {
        RakeAPI r1 = RakeAPI.getInstance(app, genToken(), Env.DEV, Logging.ENABLE);
        RakeAPI r2 = RakeAPI.getInstance(app, genToken(), Env.LIVE, Logging.ENABLE);
    }

    @Test
    public void setEndpoint() {
        /** ENDPOINT 추가 또는 변화시에는 이 테스트 코드를 반드시 변경하도록 하드코딩으로 URI 검증 */

        String CHARGED_ENDPOINT_DEV  = "https://pg.rake.skplanet.com:8443/log/track";
        String FREE_ENDPOINT_DEV     = "https://pg.rake.skplanet.com:8553/log/track";
        String CHARGED_ENDPOINT_LIVE = "https://rake.skplanet.com:8443/log/track";
        String FREE_ENDPOINT_LIVE    = "https://rake.skplanet.com:8553/log/track";

        Env e1 = Env.DEV;
        RakeAPI r1 = RakeAPI.getInstance(app, genToken(), e1, Logging.ENABLE);
        r1.setEndpoint(FREE);
        assertEquals(Endpoint.FREE_ENDPOINT_DEV, r1.getEndpoint().getURI(e1));

        Env e2 = Env.LIVE;
        RakeAPI r2 = RakeAPI.getInstance(app, genToken(), e2, Logging.ENABLE);
        r2.setEndpoint(FREE);
        assertEquals(Endpoint.FREE_ENDPOINT_LIVE, r2.getEndpoint().getURI(e2));
    }
}


