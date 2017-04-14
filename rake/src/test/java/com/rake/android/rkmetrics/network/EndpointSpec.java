package com.rake.android.rkmetrics.network;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.rake.android.rkmetrics.RakeAPI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class EndpointSpec {
    private Application app = RuntimeEnvironment.application;

    @Test
    public void ENDPOINT_생성_검증() {
        /** ENDPOINT 추가 또는 변화시에는 이 테스트 코드를 반드시 변경하도록 하드코딩으로 URI 검증 */

        String ENDPOINT_DEV = "https://pg.rake.skplanet.com:8443/log/putlog/client";
        String ENDPOINT_LIVE = "https://rake.skplanet.com:8443/log/putlog/client";

        Endpoint e1 = new Endpoint(app, RakeAPI.Env.DEV);
        assertThat(e1.getURI()).isEqualTo(ENDPOINT_DEV);

        Endpoint e2 = new Endpoint(app, RakeAPI.Env.LIVE);
        assertThat(e2.getURI()).isEqualTo(ENDPOINT_LIVE);

    }

    @Test
    public void ENDPOINT_PORT_변경_검증() {
        String ENDPOINT_DEV = "https://pg.rake.skplanet.com:8443/log/putlog/client";
        String ENDPOINT_LIVE = "https://rake.skplanet.com:8443/log/putlog/client";

        Endpoint e1 = new Endpoint(app, RakeAPI.Env.DEV);
        e1.changeURIPort(8553);
        assertThat(ENDPOINT_DEV).isNotEqualTo(e1.getURI());

        Endpoint e2 = new Endpoint(app, RakeAPI.Env.LIVE);
        e2.changeURIPort(8553);
        assertThat(ENDPOINT_LIVE).isNotEqualTo(e2.getURI());
    }

    @Test
    public void ENDPOINT_국가별_URL_셋팅_검증() {
        String ASIA_ENDPOINT_DEV = "https://pg.asia-rake.skplanet.com/log/putlog/client";
        String ENDPOINT_DEV = "https://pg.rake.skplanet.com:8443/log/putlog/client";

        // 현재 국가 설정을 태국으로 변경
        setLocale("TH", "TH");

        Endpoint e = new Endpoint(app, RakeAPI.Env.DEV);
        assertThat(e.getURI()).isEqualTo(ASIA_ENDPOINT_DEV);
        assertThat(e.getVersionSuffix()).isEqualTo("_aws");

        // 다음 Unit Test를 위해 국가설정 원복
        setLocale("KR", "KR");

        e = new Endpoint(app, RakeAPI.Env.DEV);
        assertThat(e.getURI()).isEqualTo(ENDPOINT_DEV);
        assertThat(e.getVersionSuffix()).isEqualTo("");
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
