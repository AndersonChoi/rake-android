package com.rake.android.rkmetrics.metric;

import android.app.Application;

import com.rake.android.rkmetrics.metric.model.Action;
import com.rake.android.rkmetrics.metric.model.FlushType;
import com.rake.android.rkmetrics.metric.model.Status;
import com.rake.android.rkmetrics.network.ServerResponseMetric;
import com.rake.android.rkmetrics.persistent.LogChunk;

import static com.rake.android.rkmetrics.metric.MetricUtil.*;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.Arrays;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MetricUtilSpec {

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
    public void recordFlushMetric_should_return_false_given_status_is_DONE_while_metric_token() {
        /** 메트릭값 flush 에 대한 메트릭이더라도 flush:DONE 이 아니면 기록 (e.g flush:RETRY, flush:DROP) */

        LogChunk l1 = LogChunk.create("lastId", "url", MetricUtil.BUILD_CONSTANT_METRIC_TOKEN, "chunk", 1);

        /** Status 는 최소한 1개 이상이어야 아래 루프에서 검증이 가능 */
        assertThat(Status.values().length).isGreaterThan(0);

        for (Status s: Status.values()) {
            ServerResponseMetric srm = createEmptySRM(s);
            boolean b = recordFlushMetric(app, s, FlushType.MANUAL_FLUSH, 0L, l1, srm);

            if (Status.DONE == s) assertThat(b).isFalse();
            else assertThat(b).isTrue();
        }
    }

    private ServerResponseMetric createEmptySRM(Status status) {
        ServerResponseMetric srm = ServerResponseMetric.create(null, status, "body", 0, 0L);
        assertThat(srm).isNotNull();
        return srm;
    }
}
