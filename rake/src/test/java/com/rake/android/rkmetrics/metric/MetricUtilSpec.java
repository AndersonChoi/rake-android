package com.rake.android.rkmetrics.metric;

import android.app.Application;

import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.network.ServerResponse;
import com.rake.android.rkmetrics.persistent.LogChunk;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static com.rake.android.rkmetrics.metric.MetricUtil.recordFlushMetric;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MetricUtilSpec {

    private Application app = RuntimeEnvironment.application;

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
        assertThat(MetricUtil.getURI(app)).isNotNull();
    }

    @Test
    public void isMetricToken_negative_case() {
        assertThat(MetricUtil.isMetricToken("")).isFalse();
        assertThat(MetricUtil.isMetricToken(null)).isFalse();
        assertThat(MetricUtil.isMetricToken("invalid token")).isFalse();
    }

    @Test
    public void isMetricToken_should_return_true_given_token_is_equal_to_metric_token() {
        assertThat(MetricUtil.isMetricToken(MetricUtil.BUILD_CONSTANT_METRIC_TOKEN)).isTrue();
    }

    @Test
    public void recordFlushMetric_should_return_false_given_metric_token() {
        LogChunk l1 = LogChunk.create("lastId", "url", MetricUtil.BUILD_CONSTANT_METRIC_TOKEN, "chunk", 1);

        /* Status 는 최소한 1개 이상이어야 아래 루프에서 검증이 가능 */
        assertThat(Status.values().length).isGreaterThan(0);

        for (Status s: Status.values()) {
            ServerResponse srm = createEmptySRM(s);
            boolean b = recordFlushMetric(app, "MANUAL_FLUSH", 0L, l1, srm);

            assertThat(b).isFalse();
        }
    }

    private ServerResponse createEmptySRM(Status status) {
        ServerResponse srm = ServerResponse.create("body", 0, 0L).setFlushStatus(status);
        assertThat(srm).isNotNull();
        return srm;
    }
}
