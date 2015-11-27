package com.rake.android.rkmetrics.metric;

import static com.rake.android.rkmetrics.metric.MetricLogger.*;

import android.app.Application;

import com.rake.android.rkmetrics.RakeAPI;
import com.rake.android.rkmetrics.util.functional.Callback;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import static org.assertj.core.api.Assertions.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.InvalidClassException;

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
    public void MetricLogger_write_는_예외를_잡아야서_exception_type_과_stacktrace_를_기록해야함()
            throws JSONException {

        logger = MetricLogger.getInstance(app);

        final RuntimeException e = new RuntimeException("intended");

        RakeClientMetricSentinelShuttle shuttle =
                logger.write(new Callback<RakeClientMetricSentinelShuttle, Action>() {
                    @Override
                    public Action execute(RakeClientMetricSentinelShuttle arg) {
                        throw e;
                    }
                });

        JSONObject body = shuttle.getBody();
        String exceptionType = body.getString(FIELD_NAME_EXCEPTION_TYPE);
        String stacktrace = body.getString(FIELD_NAME_STACKTRACE_STACKTRACE);

        assertThat(exceptionType).isNotNull();
        assertThat(stacktrace).isNotNull();

        assertThat(exceptionType).isEqualTo(getExceptionType(e));
    }

    @Test
    public void 테스트_getExceptionType() {
        Exception e = new IllegalArgumentException("e");

        assertThat(getExceptionType(null)).isNull();
        assertThat(getExceptionType(e)).isNotNull();
    }

    @Test
    public void 테스트_getStackTraceString() {
        Exception e = new InvalidClassException("e");

        assertThat(getStacktraceString(null)).isNull();
        assertThat(getStacktraceString(e)).isNotNull();
    }

    @Test
    public void MetricLogger_initializeShuttle_은_셔틀의_키와_바디를_초기화_해야함() {

    }

    @Test
    public void MetricLogger_write_는_셔틀에_operation_time_을_기록해야함_Action_이_Track_이_아닐경우() {

    }

    @Test
    public void MetricLogger_write_는_셔틀에_operation_time_list_을_기록해야함_Action_이_Track_일때() {

    }
}
