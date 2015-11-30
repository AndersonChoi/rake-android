package com.rake.android.rkmetrics.metric;

import static com.rake.android.rkmetrics.metric.model.Body.*;
import static com.rake.android.rkmetrics.metric.ShuttleProfiler.*;

import android.app.Application;

import com.rake.android.rkmetrics.util.functional.Callback;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import static org.assertj.core.api.Assertions.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
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
    public void measureOperationTime_는_예외를_잡아야서_exception_type_과_stacktrace_를_기록해야함()
            throws JSONException {

        final RuntimeException e = new RuntimeException("intended");

        RakeClientMetricSentinelShuttle shuttle = new RakeClientMetricSentinelShuttle();
        MetricLogger.measureOperationTime(shuttle, new Callback<RakeClientMetricSentinelShuttle, Void>() {
            @Override
            public Void execute(RakeClientMetricSentinelShuttle arg) {
                throw e;
            }
        });

        JSONObject body = shuttle.getBody();
        String exceptionType = body.getString(FIELD_NAME_EXCEPTION_TYPE);
        String stacktrace = body.getString(FIELD_NAME_STACKTRACE);

        assertThat(exceptionType).isNotNull();
        assertThat(stacktrace).isNotNull();

        assertThat(exceptionType).isEqualTo(createExceptionType(e));
    }

    @Test
    public void measureOperationTime_은_연산시간을_기록해야_함() {
        RakeClientMetricSentinelShuttle shuttle = new RakeClientMetricSentinelShuttle();

        final long OPERATION_TIME = 200L;

        MetricLogger.measureOperationTime(shuttle, new Callback<RakeClientMetricSentinelShuttle, Void>() {
            @Override
            public Void execute(RakeClientMetricSentinelShuttle arg) {

                try {
                    Thread.sleep(OPERATION_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });


        assertThat(hasBodyValue(shuttle, FIELD_NAME_OPERATION_TIME, new Callback<Long, Boolean>() {
            @Override
            public Boolean execute(Long operationTime) {
                return operationTime > OPERATION_TIME;
            }
        })).isTrue();
    }

    public void TODO() {
        new RuntimeException("TODO");
    }
}
