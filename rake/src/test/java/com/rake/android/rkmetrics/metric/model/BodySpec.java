package com.rake.android.rkmetrics.metric.model;

import com.rake.android.rkmetrics.util.ExceptionUtil;
import com.skplanet.pdp.sentinel.shuttle.RakeClientMetricSentinelShuttle;
import static com.rake.android.rkmetrics.shuttle.ShuttleProfiler.*;
import static com.rake.android.rkmetrics.metric.model.Header.*;
import static com.rake.android.rkmetrics.metric.model.Body.*;

import static org.assertj.core.api.Assertions.*;

import org.json.JSONObject;
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
        assertThat(hasHeaderValue(shuttle, HEADER_NAME_ACTION, EMPTY_FIELD_VALUE)).isTrue();
        assertThat(hasHeaderValue(shuttle, HEADER_NAME_STATUS, EMPTY_FIELD_VALUE)).isTrue();

        /* body validation */
        assertThat(bodyString).isEqualTo(EMPTY_BODY_STRING);
        assertThat(hasBodyValue(shuttle, BODY_NAME_EXCEPTION_TYPE, "")).isFalse();
        assertThat(hasBodyValue(shuttle, BODY_NAME_STACKTRACE, "")).isFalse();
    }

    @Test
    public void fillCommonBodyFields_should_return_false_given_shuttle_is_null() {
        Body b = createEmptyBody();
        assertThat(b.fillCommonBodyFields(null)).isFalse();
    }

    @Test
    public void fillCommonBodyFields_should_set_existing_header_fields_to_shuttle() {
        Body b = createEmptyBody();

        Exception e = new IllegalArgumentException("example exception");

        b.setExceptionInfo(e);

        RakeClientMetricSentinelShuttle shuttle = new RakeClientMetricSentinelShuttle();
        b.fillCommonBodyFields(shuttle);

        hasBodyValue(shuttle, BODY_NAME_EXCEPTION_TYPE, ExceptionUtil.createExceptionType(e));
        hasBodyValue(shuttle, BODY_NAME_STACKTRACE, ExceptionUtil.createStacktrace(e));
    }

    private Body createEmptyBody() {
        return new Body() {
            @Override
            public JSONObject toJSONObject() {
                return null;
            }

            @Override
            public String getMetricType() {
                return "EMPTY BODY";
            }
        };

    }
}
