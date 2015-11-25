package com.rake.android.rkmetrics.metric;

import static org.assertj.core.api.Assertions.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MetricLoggerSpec {

    static MetricLogger logger;

    @BeforeClass
    public static void setUpAll() {
       logger = MetricLogger.getInstance();
    }

    @Test
    public void MetricLogger_생성_테스트() {
        assertThat(MetricLogger.getInstance()).isNotNull();
    }

    @Test
    public void MetricLogger_싱글턴_테스트() {
        assertThat(MetricLogger.getInstance()).isEqualTo(MetricLogger.getInstance());
    }
}
