package com.rake.android.rkmetrics;

import static com.rake.android.rkmetrics.network.Endpoint.*;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;
import com.rake.android.rkmetrics.RakeAPI.Env;
import com.rake.android.rkmetrics.RakeAPI.AutoFlush;
import com.rake.android.rkmetrics.RakeAPI.Logging;


import android.app.Application;

import com.rake.android.rkmetrics.network.Endpoint;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class RakeAPISpec {

    Application app = RuntimeEnvironment.application;
    RakeAPI rake = RakeAPI.getInstance(
            app,
            "exampleToken",
            RakeAPI.Env.DEV,
            RakeAPI.Logging.ENABLE
            );

    @Test
    public void getInstance_should_throw_IllegalArgumentException_given_null_arg() {
        // TODO
    }

    @Test
    public void setFlushInterval() {
        long defaultInterval = MessageLoop.DEFAULT_FLUSH_INTERVAL;
        long newInterval = defaultInterval + 1000;

        assertThat(MessageLoop.DEFAULT_FLUSH_INTERVAL).isEqualTo(RakeAPI.getFlushInterval());

        RakeAPI.setFlushInterval(newInterval);

        assertThat(newInterval).isEqualTo(RakeAPI.getFlushInterval());
    }

    @Test
    public void Env_Live_시_라이브서버_과금_URL_로_세팅되어야함() {
        Env e = Env.DEV;
        RakeAPI r = RakeAPI.getInstance(app, TestUtil.genToken(), e, Logging.ENABLE);
        assertThat(Endpoint.CHARGED_ENDPOINT_DEV).isEqualTo(r.getEndpoint().getURI(e));
    }

    @Test
    public void Env_Dev_시_개발서버_과금_URL_로_세팅되어야함() {
        Env e = Env.LIVE;
        RakeAPI r = RakeAPI.getInstance(app, TestUtil.genToken(), e, Logging.ENABLE);
        assertThat(Endpoint.CHARGED_ENDPOINT_LIVE).isEqualTo(r.getEndpoint().getURI(e));
    }

    @Test
    public void 복수의_Env_를_지원해야함() {
        RakeAPI r1 = RakeAPI.getInstance(app, TestUtil.genToken(), Env.DEV, Logging.ENABLE);
        RakeAPI r2 = RakeAPI.getInstance(app, TestUtil.genToken(), Env.LIVE, Logging.ENABLE);
    }

    @Test
    public void setEndpoint() {
        Env e1 = Env.DEV;
        RakeAPI r1 = RakeAPI.getInstance(app, TestUtil.genToken(), e1, Logging.ENABLE);
        r1.setEndpoint(FREE);
        assertThat(Endpoint.FREE_ENDPOINT_DEV).isEqualTo(r1.getEndpoint().getURI(e1));

        Env e2 = Env.LIVE;
        RakeAPI r2 = RakeAPI.getInstance(app, TestUtil.genToken(), e2, Logging.ENABLE);
        r2.setEndpoint(FREE);
        assertThat(Endpoint.FREE_ENDPOINT_LIVE).isEqualTo(r2.getEndpoint().getURI(e2));
    }

    @Test
    public void test_Endpoint_changed() {
        /** ENDPOINT 추가 또는 변화시에는 이 테스트 코드를 반드시 변경하도록 하드코딩으로 URI 검증 */

        String CHARGED_ENDPOINT_DEV  = "https://pg.rake.skplanet.com:8443/log/track";
        String FREE_ENDPOINT_DEV     = "https://pg.rake.skplanet.com:8553/log/track";
        String CHARGED_ENDPOINT_LIVE = "https://rake.skplanet.com:8443/log/track";
        String FREE_ENDPOINT_LIVE    = "https://rake.skplanet.com:8553/log/track";

        assertThat(CHARGED.getURI(Env.DEV)).isEqualTo(CHARGED_ENDPOINT_DEV);
        assertThat(CHARGED.getURI(Env.LIVE)).isEqualTo(CHARGED_ENDPOINT_LIVE);
        assertThat(FREE.getURI(Env.DEV)).isEqualTo(FREE_ENDPOINT_DEV);
        assertThat(FREE.getURI(Env.LIVE)).isEqualTo(FREE_ENDPOINT_LIVE);
    }

    @Test
    public void 최초_AutoFlush_는_ON_이어야함() {
        RakeAPI r1 = RakeAPI.getInstance(app, TestUtil.genToken(), Env.DEV, Logging.ENABLE);
        RakeAPI r2 = RakeAPI.getInstance(app, TestUtil.genToken(), Env.LIVE, Logging.ENABLE);

        assertThat(AutoFlush.ON).isEqualTo(RakeAPI.getAutoFlush());
        assertThat(AutoFlush.ON).isEqualTo(RakeAPI.getAutoFlush());

        RakeAPI.setAutoFlush(AutoFlush.OFF);
        assertThat(AutoFlush.OFF).isEqualTo(RakeAPI.getAutoFlush());
    }

    @Test
    public void test_getDefaultProperties() throws JSONException {
        String token = TestUtil.genToken();
        JSONObject defaultProps = RakeAPI.getDefaultProps(app, Env.DEV, token, new Date());

        /** 교차 검증, defaultProps Keys <-> DEFAULT_PROPERTY_NAMES */
        for (Iterator<?> keys = defaultProps.keys(); keys.hasNext(); ) {
            String key = (String) keys.next();
            assertThat(DEFAULT_PROPERTY_NAMES.contains(key)).isTrue();
        }

        for (String key : DEFAULT_PROPERTY_NAMES) {
            assertThat(defaultProps.has(key)).isTrue();
        }
    }
}

