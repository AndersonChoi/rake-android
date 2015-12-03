package com.rake.android.rkmetrics.metric.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.robolectric.annotation.Config.*;
import static org.assertj.core.api.Assertions.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = NONE)
public class MetricSpec {
    @Test
    public void create_Metric() {
        FlushMetric m1 = new FlushMetric();
        m1.toJSONObject();

        InstallMetric m2 = new InstallMetric();
        m2.toJSONObject();

        EmptyMetric m3 = new EmptyMetric();
        m3.toJSONObject();
    }

    @Test
    public void getMetricType_should_not_return_null() {
        FlushMetric m1 = new FlushMetric();
        assertThat(m1.getMetricType()).isNotNull();

        InstallMetric m2 = new InstallMetric();
        assertThat(m2.getMetricType()).isNotNull();

        EmptyMetric m3 = new EmptyMetric();
        assertThat(m3.getMetricType()).isNotNull();
    }

    @Test
    public void getMetricType_fromEmptyMetric_without_action_and_status_should_return_() {
        /* ":UNKNOWN" */

        assertThat(new EmptyMetric().getMetricType())
                .isEqualTo(Action.EMPTY.getValue() + ":" + Status.UNKNOWN.getValue());
    }
}
