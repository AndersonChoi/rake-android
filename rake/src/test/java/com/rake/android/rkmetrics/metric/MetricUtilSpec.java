package com.rake.android.rkmetrics.metric;

import android.app.Application;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MetricUtilSpec {

    MetricUtil logger;
    Application app = RuntimeEnvironment.application;

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
    }

    @Test
    public void createTransactionId() {
        String transactionId = MetricUtil.createTransactionId();

        assertThat(MetricUtil.TRANSACTION_ID).isNotNull();
        assertThat(transactionId).isNotNull();

        assertThat(transactionId).doesNotContain("-");
        assertThat(MetricUtil.TRANSACTION_ID).doesNotContain("-");
    }

    @Test
    public void getEndpoint() {
        assertThat(MetricUtil.getURI()).isNotNull();
    }
}
