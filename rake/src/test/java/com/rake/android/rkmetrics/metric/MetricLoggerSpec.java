package com.rake.android.rkmetrics.metric;

import android.app.Application;

import com.rake.android.rkmetrics.RakeAPI;

import static org.assertj.core.api.Assertions.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MetricLoggerSpec {

    static MetricLogger logger;
    static Application app = RuntimeEnvironment.application;

    @BeforeClass
    public static void setUpAll() {
       logger = MetricLogger.getInstance(app);
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
        // TODO

    }

    @Test
    public void MetricLogger_write_는_예외를_잡아야함(){

    }

    @Test
    public void MetricLogger_write_는_예외를_잡아_ERROR_이벤트를_셔틀에_기록해야함() {

    }

    @Test
    public void MetricLogger_write_는_셔틀에_operation_time_을_기록해야함_Action_이_Track_이_아닐경우() {

    }

    @Test
    public void MetricLogger_write_는_셔틀에_operation_time_list_을_기록해야함_Action_이_Track_일때() {

    }
}
