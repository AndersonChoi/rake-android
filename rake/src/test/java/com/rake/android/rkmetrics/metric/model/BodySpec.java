package com.rake.android.rkmetrics.metric.model;

import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;
import static com.rake.android.rkmetrics.metric.model.Header.*;
import static com.rake.android.rkmetrics.metric.model.Body.*;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BodySpec {

    @Test
    public void initializeShuttle_은_셔틀의_키와_바디를_초기화_해야함() {
        RakeClientMetricSentinelShuttle shuttle = new RakeClientMetricSentinelShuttle();

        /* header */
        shuttle.action("install");
        shuttle.status("status");

        /* body */
        shuttle.exception_type(new OutOfMemoryError("").getClass().getSimpleName());

        shuttle = Body.initializeShuttle(shuttle);
        String bodyString = shuttle.bodyToString();

        /* header validation */
        assertThat(hasHeaderValue(shuttle, FIELD_NAME_ACTION, EMPTY_HEADER_VALUE)).isTrue();
        assertThat(hasHeaderValue(shuttle, FIELD_NAME_STATUS, EMPTY_HEADER_VALUE)).isTrue();

        /* body validation */
        assertThat(bodyString).isEqualTo(EMPTY_BODY_STRING);
        assertThat(hasBodyValue(shuttle, FIELD_NAME_EXCEPTION_TYPE, "")).isFalse();
        assertThat(hasBodyValue(shuttle, FIELD_NAME_STACKTRACE, "")).isFalse();
    }

}
