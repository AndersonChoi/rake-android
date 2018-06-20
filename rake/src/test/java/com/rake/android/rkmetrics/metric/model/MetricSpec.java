package com.rake.android.rkmetrics.metric.model;

import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(RobolectricTestRunner.class)
public class MetricSpec {
    @Test
    public void create_Metric() {
        Metric metric = new Metric.Builder(new RakeClientMetricSentinelShuttle()).build();
        assertThat(metric).isNotNull();
    }

    @Test
    public void getMetricType_should_not_return_null() {
        Metric metric = new Metric.Builder(new RakeClientMetricSentinelShuttle()).build();
        assertThat(metric.getMetricType()).isNotNull();
    }

    @Test
    public void getMetricType_without_action_and_status_should_return_() {
        Metric metric = new Metric.Builder(new RakeClientMetricSentinelShuttle()).build();
        assertThat(metric.getMetricType()).isEqualTo(Action.EMPTY.getValue() + ":" + Status.UNKNOWN.getValue());
    }

    @Test
    public void toJSONObject_should_not_return_null() {
        Metric metric = new Metric.Builder(new RakeClientMetricSentinelShuttle()).build();
        assertThat(metric.toJSONObject()).isNotNull();
    }
}
