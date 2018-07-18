package com.rake.android.rkmetrics;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.rake.android.rkmetrics.RakeAPI.AutoFlush;
import com.rake.android.rkmetrics.RakeAPI.Env;
import com.rake.android.rkmetrics.RakeAPI.Logging;
import com.rake.android.rkmetrics.config.RakeConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Iterator;
import java.util.Locale;

import static com.rake.android.rkmetrics.TestUtil.failWhenSuccess;
import static com.rake.android.rkmetrics.TestUtil.genToken;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfilerValueChecker.DEFAULT_PROPERTY_NAMES;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class RakeAPISpec {

    private Application app = RuntimeEnvironment.application;

    @Test()
    public void getInstance_should_throw_IllegalArgumentException_given_null_arg() {

        failWhenSuccess(IllegalArgumentException.class, new TestCallback() {
            @Override
            public <R> R execute() {
                RakeAPI.getInstance(null, null, null, null);
                return null;
            }
        });

        failWhenSuccess(IllegalArgumentException.class, new TestCallback() {
            @Override
            public <R> R execute() {
                RakeAPI.getInstance(null, genToken(), Env.DEV, Logging.DISABLE);
                return null;
            }
        });

        failWhenSuccess(IllegalArgumentException.class, new TestCallback() {
            @Override
            public <R> R execute() {
                RakeAPI.getInstance(app, null, Env.DEV, Logging.DISABLE);
                return null;
            }
        });

        failWhenSuccess(IllegalArgumentException.class, new TestCallback() {
            @Override
            public <R> R execute() {
                RakeAPI.getInstance(app, genToken(), null, Logging.DISABLE);
                return null;
            }
        });

        failWhenSuccess(IllegalArgumentException.class, new TestCallback() {
            @Override
            public <R> R execute() {
                RakeAPI.getInstance(app, genToken(), Env.DEV, null);
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void getInstance_should_throw_IllegalArgumentException_given_token_is_empty_string() {
        RakeAPI.getInstance(app, "", Env.DEV, Logging.DISABLE);
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
    public void 복수의_Env_를_지원해야함() {
        RakeAPI r1 = RakeAPI.getInstance(app, TestUtil.genToken(), Env.DEV, Logging.ENABLE);
        RakeAPI r2 = RakeAPI.getInstance(app, TestUtil.genToken(), Env.LIVE, Logging.ENABLE);

        assertThat(r1.getServerURL()).isNotEqualTo(r2.getServerURL());
    }

    @Test
    public void test_setFreeEndpointPort() {
        String CHARGED_ENDPOINT_DEV = "https://pg.rake.skplanet.com:8663/log/putlog/client";
        String CHARGED_ENDPOINT_LIVE = "https://rake.skplanet.com:8663/log/putlog/client";


        RakeAPI r1 = RakeAPI.getInstance(app, TestUtil.genToken(), Env.DEV, Logging.ENABLE);
        r1.setServerPort(8663);

        assertThat(CHARGED_ENDPOINT_DEV).isEqualTo(r1.getServerURL());

        // restore for the next test cases
        r1.setServerPort(8443);
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
        JSONObject defaultProps = RakeAPI.getDefaultProps(app, token);

        /* 교차 검증, defaultProps Keys <-> DEFAULT_PROPERTY_NAMES */
        for (Iterator<?> keys = defaultProps.keys(); keys.hasNext(); ) {
            String key = (String) keys.next();
            assertThat(DEFAULT_PROPERTY_NAMES.contains(key)).isTrue();
            System.out.println();
        }

        for (String key : DEFAULT_PROPERTY_NAMES) {
            assertThat(defaultProps.has(key)).isTrue();
            System.out.println(key);
        }
    }

    @Test
    public void Locale_변경_Endpoint_변경_검증() {
        String ASIA_ENDPOINT_DEV = "https://pg.asia-rake.skplanet.com/log/putlog/client";
        String ENDPOINT_DEV = "https://pg.rake.skplanet.com:8443/log/putlog/client";

        setLocale("TH", "TH");
        RakeAPI r = RakeAPI.getInstance(app, TestUtil.genToken(), Env.DEV, Logging.ENABLE);
        assertThat(r.getServerURL()).isEqualTo(ASIA_ENDPOINT_DEV);
        assertThat(RakeAPI.getLibVersion()).isEqualTo(RakeConfig.RAKE_LIB_VERSION + "_aws");

        // Locale이 바뀐 상태에서 인스턴스를 새로 생성했을 경우 URL이 바뀌어 있어야 함
        setLocale("KR", "KR");
        r = RakeAPI.getInstance(app, TestUtil.genToken(), Env.DEV, Logging.ENABLE);
        assertThat(r.getServerURL()).isEqualTo(ENDPOINT_DEV);
        assertThat(RakeAPI.getLibVersion()).isEqualTo(RakeConfig.RAKE_LIB_VERSION);
    }

    private void setLocale(String language, String country) {
        Locale locale = new Locale(language, country);
        // here we update locale for date formatters
        Locale.setDefault(locale);
        // here we update locale for app resources
        Resources res = app.getResources();
        Configuration config = res.getConfiguration();
        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}

