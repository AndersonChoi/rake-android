package com.rake.android.rkmetrics.metric;

import android.app.Application;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.util.functional.Callback;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class MetricLoggerSpec {

    MetricLogger logger;
    Application app = RuntimeEnvironment.application;

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
        MetricLogger.initialize();
    }

    @Test
    public void MetricLogger_생성_테스트() {
        assertThat(MetricLogger.getInstance(app)).isNotNull();
    }

    @Test
    public void MetricLogger_싱글턴_테스트() {
        assertThat(MetricLogger.getInstance(app)).isEqualTo(MetricLogger.getInstance(app));
    }

    @Test
    public void MetricLogger_생성시_Rake_인스턴스도_생성되야함() {
        logger = MetricLogger.getInstance(app);
        assertThat(logger.getRake()).isNotNull();
    }

    @Test
    public void MetricLogger_write_는_예외를_잡아야서_exception_type_과_stacktrace_를_기록해야함(){
        logger = MetricLogger.getInstance(app);

        RakeClientMetricSentinelShuttle shuttle =

                logger.write(new Callback<RakeClientMetricSentinelShuttle, Action>() {
            @Override
            public Action execute(RakeClientMetricSentinelShuttle arg) {
                throw new RuntimeException("intended");
            }
        });

        
    }

    @Test
    public void MetricLogger_write_는_예외를_잡아_ERROR_이벤트를_셔틀에_기록해야함() {
        logger = MetricLogger.getInstance(app);

    }

    @Test
    public void MetricLogger_write_는_셔틀에_operation_time_을_기록해야함_Action_이_Track_이_아닐경우() {

    }

    @Test
    public void MetricLogger_write_는_셔틀에_operation_time_list_을_기록해야함_Action_이_Track_일때() {

    }
}
