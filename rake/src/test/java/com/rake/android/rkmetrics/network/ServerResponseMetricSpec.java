package com.rake.android.rkmetrics.network;

import static com.rake.android.rkmetrics.metric.model.FlushResult.*;

import static org.assertj.core.api.Assertions.*;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ServerResponseMetricSpec {

    @Test
    public void create_생성시_TransmissionResult만_NULL_일경우_NULL_리턴() {

        ServerResponseMetric s1 =
                ServerResponseMetric.create(null, null, "body", 1, 0L);

        ServerResponseMetric s2 =
                ServerResponseMetric.create(null, FAILURE_RECOVERABLE, null, 1, 0L);

        ServerResponseMetric s3 =
                ServerResponseMetric.create(null, FAILURE_RECOVERABLE, "body", 1, 0L);

        assertThat(s1).isNull();

        assertThat(s2).isNotNull();
        assertThat(s3).isNotNull();
    }
}
